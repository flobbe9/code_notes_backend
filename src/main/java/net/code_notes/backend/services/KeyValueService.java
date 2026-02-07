package net.code_notes.backend.services;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static net.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringEscapeUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.abstracts.AbstractService;
import net.code_notes.backend.abstracts.NoteInputType;
import net.code_notes.backend.entities.KeyValue;
import net.code_notes.backend.entities.NoteInput;
import net.code_notes.backend.repositories.KeyValueRepository;
import net.code_notes.backend.repositories.NoteInputRepository;

@Service
@Log4j2
public class KeyValueService extends AbstractService<KeyValue> {
    
    @Autowired
    private KeyValueRepository keyValueRepository;
    
    @Autowired
    private NoteInputRepository noteInputRepository;


    @Override
    protected KeyValue saveNew(KeyValue entity) throws ResponseStatusException, IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(entity);

        validateAndThrow(entity);

        return this.keyValueRepository.save(entity);
    }

    @Override
    protected KeyValue update(KeyValue entity) throws ResponseStatusException, IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(entity);

        validateAndThrow(entity);

        return this.keyValueRepository.save(entity);
    }

    @Override
    protected boolean validateAndThrow(KeyValue entity) {
        super.validateAndThrow(entity);

        // unique check
        if (entity.getId() == null) {
            if (this.keyValueRepository.existsBy_key(entity.get_key()))
                throw new ResponseStatusException(CONFLICT, "Duplicate keyValue with _key '%s'".formatted(entity.get_key()));

        } else {
            if (this.keyValueRepository.existsBy_keyAndIdNotIn(entity.get_key(), List.of(entity.getId())))
                throw new ResponseStatusException(CONFLICT, "Duplicate keyValue with _key '%s'".formatted(entity.get_key()));
        }

        return true;
    }

    // TODO: remove this once migrated
    public Optional<KeyValue> loadByKey(String key) {
        if (isBlank(key))
            return Optional.ofNullable(null);
        
        return this.keyValueRepository.findBy_key(key);
    }

    @Transactional
    public void migrateHtml() {
        log.info("Migrating html input values to plain text input values...");

        String migrationKey = "migrateNoteInputHtml";
        KeyValue migrationKeyValue = loadByKey(migrationKey).orElse(new KeyValue(migrationKey, "false"));

        // case: alredy migrated
        if (migrationKeyValue.get_value().equals("true")) {
            log.info("Migration already completed. Not changing anything.");
            return;
        }

        List<NoteInput> noteInputs = null;
        int pageNumber = 0;
        int pageSize = 100;
        // load note inputs 100 at a time
        while ((noteInputs = this.noteInputRepository.findAllByTypeIn(List.of(NoteInputType.PLAIN_TEXT, NoteInputType.CODE_WITH_VARIABLES), PageRequest.of(pageNumber, pageSize))).size() > 0) {
            pageNumber++;
            noteInputs.forEach(noteInput -> migrateNoteInputHtml(noteInput));
            log.info("page {}, size {}", pageNumber, noteInputs.size());
        }

        log.info("Finished migration");

        migrationKeyValue.set_value("true");
        this.keyValueRepository.save(migrationKeyValue);
    }

    private void migrateNoteInputHtml(NoteInput noteInput) {
        assertArgsNotNullAndNotBlankOrThrow(noteInput);

        PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements()
            .toFactory();

        String html = noteInput.getValue();
        switch (noteInput.getType()) {
            case PLAIN_TEXT:
                if (isBlank(html))
                    return;
                POLICY = new HtmlPolicyBuilder()
                    .allowElements("code")
                    .toFactory();
    
                html = "<div>Gradle <code>build.gradle</code> </div>";
        
                html = html
                    .replace("</div>", "\n")
                    .replace("<br>", "\n")
                    .replace("<br />", "\n");
        
                html = POLICY.sanitize(html);
        
                html = html
                    .replace("<code>", "`")
                    .replace("</code>", "`");
        
                html = StringEscapeUtils.unescapeHtml4(html);
                break;

            case CODE_WITH_VARIABLES:
                if (isBlank(html))
                    return;

                POLICY = new HtmlPolicyBuilder()
                    .allowElements("input")
                    .allowAttributes("placeholder").onElements("input")
                    .toFactory();
        
                html = html
                    .replace("</div>", "\n")
                    .replace("<br>", "\n")
                    .replace("<br />", "\n");
        
                html = POLICY.sanitize(html);
        
                html = html
                    .replace("<input ", "$[[")
                    .replace("placeholder=\"", "")
                    .replace("\" />", "]]");
        
                html = StringEscapeUtils.unescapeHtml4(html);
                break;

            default:
                throw new IllegalArgumentException("Type '%s' of noteInput is not implemented".formatted(noteInput.getType()));
        }

        log.debug("migrated input value from %s to %s".formatted(noteInput.getValue(), html));
        noteInput.setValue(html);
    }
}
