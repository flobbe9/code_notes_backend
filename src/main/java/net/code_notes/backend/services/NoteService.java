package net.code_notes.backend.services;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static net.code_notes.backend.helpers.Utils.assertArgsNullOrBlank;
import static net.code_notes.backend.helpers.Utils.isBlank;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.abstracts.AbstractService;
import net.code_notes.backend.abstracts.NoteInputType;
import net.code_notes.backend.dto.NoteInputValueDto;
import net.code_notes.backend.dto.SearchNoteDto;
import net.code_notes.backend.dto.SearchNoteInputDto;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.Note;
import net.code_notes.backend.helpers.Utils;
import net.code_notes.backend.helpers.search.SearchStringUtils;
import net.code_notes.backend.repositories.NoteRepository;


/**
 * @since 0.0.1
 */
@Service
@Log4j2
public class NoteService extends AbstractService<Note> {
    
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private NoteInputService noteInputService;
    

    /**
     * @return all notes of the app user currently logged in
     * @throws ResponseStatusException
     * @deprecated use {@link #loadByCurrentAppUserOrderByCreatedDescPageable} instead
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public List<Note> getAllByCurrentAppUser() throws ResponseStatusException {
        AppUser appUser = this.appUserService.getCurrent();

        return this.noteRepository.findAllByAppUserEmailOrderByCreatedDesc(appUser.getEmail());
    }

    public long countByCurrentAppUser() {
        AppUser currentAppUser = this.appUserService.getCurrent();

        return this.noteRepository.countByAppUserEmail(currentAppUser.getEmail());
    }

    /**
     * Loads notes of current app user and matches {@code searchPhrase} agains {@code note.title} and {@code note.codeNoteInputsWithVars.first.value}
     * only returning results that have at least one match AND contain all {@code tagNames}.<p>
     * 
     * If {@code searchPhrase} is not specified, just apply {@code tagNames} and if those are missing too, load notes unfiltered.<p>
     * 
     * Sort by created desc and search accuracy (prioritise search accuracy).
     *  
     * @param pageRequest for pagination
     * @param searchPhrase e.g. user searchbar input
     * @param tagNames 
     * @return matching notes or empty list, never {@code null}
     */
    @NonNull
    public List<Note> loadByCurrentAppUserSortedAndSearch(@NonNull PageRequest pageRequest, String searchPhrase, List<String> tagNames) {
        assertArgsNotNullAndNotBlankOrThrow(pageRequest);

        AppUser currentAppUser = this.appUserService.getCurrent();
        boolean isFilterByTags = tagNames != null && !tagNames.isEmpty();

        // case: no search phrase
        if (isBlank(searchPhrase)) {
            // case: no search input at all, just sort and return pageable
            if (!isFilterByTags)
                return loadByCurrentAppUserSorted(pageRequest);

            // case: only filter by tags, sort and pageable
            return this.noteRepository.findByAppUserEmailAndTags_NameInOrderByCreatedDesc(currentAppUser.getEmail(), tagNames, pageRequest);
        }
            
        // load minimized notes
        List<SearchNoteDto> noteDtos = null;
        if (isFilterByTags)
            noteDtos = this.noteRepository.findByAppUserEmailAndTags_NameIn(currentAppUser.getEmail(), tagNames);
        else
            noteDtos = this.noteRepository.findByAppUserEmail(currentAppUser.getEmail());

        Map<SearchNoteDto, Double> resultNoteDtos = new LinkedHashMap<>();        

        // search
        noteDtos.stream()
            .forEach(noteDto -> {
                // match note.title
                double ratingPoints = SearchStringUtils.matchPhrases(searchPhrase, noteDto.getTitle());

                // match note.codeNoteInputsWithVars.first.value
                double ratingPointsCodeInputWithVariables = matchFirstCodeNoteInputWithVariablesValue(noteDto, searchPhrase);
                // case: is a better match than note.title
                if (ratingPointsCodeInputWithVariables > ratingPoints)
                    ratingPoints = ratingPointsCodeInputWithVariables;
                
                // only show matches
                if (ratingPoints > 0)
                    resultNoteDtos.put(noteDto, ratingPoints);
            });

        // sort by created desc and rating points (prioritise rating points)
        List<Entry<SearchNoteDto, Double>> sortedNoteDtos = resultNoteDtos.entrySet().stream()
            .sorted((entry1, entry2) -> entry2.getKey().getCreated().compareTo(entry1.getKey().getCreated()))
            .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
            .toList();

        return Utils
            // paginate    
            .paginate(sortedNoteDtos, pageRequest.getPageNumber(), pageRequest.getPageSize())
            // load actual notes
            .stream()
            .map(entry -> loadById(entry.getKey().getId()))
            .toList();
    }

    /**
     * Match {@code searchPhrase} against the firt code note input with vars if present. Use the inputs sanitized value.
     * 
     * @param noteDto possibly containing the code note input with vars
     * @param searchPhrase to match input value against
     * @return the rating points returned by {@code SearchUtils.matchPhrases}, 0 if invalid args or no input present
     */
    private double matchFirstCodeNoteInputWithVariablesValue(SearchNoteDto noteDto, String searchPhrase) {
        if (assertArgsNullOrBlank(noteDto, searchPhrase))
            return 0;

        // find input with vars
        SearchNoteInputDto firstCodeNoteInputWithVariablesDto = noteDto.getNoteInputs()
            .stream()
            .filter(noteInput -> noteInput.getType().equals(NoteInputType.CODE_WITH_VARIABLES))
            .findFirst()
            .orElse(null);

        double ratingPointsCodeInputWithVariables = 0;

        // case: found an input with vars
        if (firstCodeNoteInputWithVariablesDto != null) {
            // load value
            NoteInputValueDto firstCodeNoteInputWithVariables = this.noteInputService.loadValueById(firstCodeNoteInputWithVariablesDto.getId());

            // sanitize all html
            String value = firstCodeNoteInputWithVariables.getValue();
            PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements()
                .toFactory();
            if (!isBlank(value))
                value = policy.sanitize(value);

            ratingPointsCodeInputWithVariables = SearchStringUtils.matchPhrases(searchPhrase, value);
        }

        return ratingPointsCodeInputWithVariables;
    }

    /**
     * Sort by created desc.
     * 
     * @param pageRequest 0
     * @return a page of notes related to the current app user
     * @throws IllegalArgumentException
     * @throws ResponseStatusException
     */
    private List<Note> loadByCurrentAppUserSorted(@NonNull PageRequest pageRequest) throws ResponseStatusException {
        assertArgsNotNullAndNotBlankOrThrow(pageRequest);

        AppUser appUser = this.appUserService.getCurrent();

        return this.noteRepository.findByAppUserEmailOrderByCreatedDesc(appUser.getEmail(), pageRequest);
    }
    
    /**
     * Save or create given {@code note} and reference it to given {@code appUser}.
     * Also save or delete tags if necessary.
     * 
     * @param note to save. {@code appUser} field might not be present because of {@code @JsonIgnore}
     * @return saved {@code note}
     * @throws ResponseStatusException if note is invalid or not logged in
     * @throws IllegalArgumentException if a param is {@code null}
     */
    @Override
    public Note save(Note note) throws ResponseStatusException, IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(note);

        validateAndThrow(note);

        AppUser currentAppUser = this.appUserService.loadCurrentFromDb();

        setIgnoredFields(note, currentAppUser);

        this.tagService.handleSaveNote(note, currentAppUser);

        note = this.noteRepository.save(note);

        this.tagService.removeOrphanTags(currentAppUser);

        return note;
    }

    @Override
    protected Note saveNew(Note note) throws ResponseStatusException, IllegalArgumentException {
        return save(note);
    }

    @Override
    protected Note update(Note note) throws ResponseStatusException, IllegalArgumentException {
        return save(note);
    }

    /**
     * Save all notes from given list using {@link #save(Note)} method.
     * 
     * @param notes may be empty
     * @return list of saved notes, empty list if {@code notes} is empty
     * @throws ResponseStatusException see {@link #save(Note)}
     * @throws IllegalArgumentException if arg is null
     */
    public Collection<Note> saveAll(Collection<Note> notes) throws ResponseStatusException, IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(notes);

        return notes
            .stream()
            .map(note -> save(note))
            .toList();
    }

    /**
     * Get given {@code note} with fields annotated with {@code @JsonIgnore}.
     * 
     * @param note to complet (will be altered)
     * @param appUser to set {@code note.appUser} to
     * @return given {@code note}
     * @throws IllegalArgumentException
     */
    private Note setIgnoredFields(Note note, AppUser appUser) throws IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(note, appUser);

        note.setAppUser(appUser);

        return note;
    }

    /**
     * @param id
     * @return note with given id or {@code null}
     */
    public Note loadById(@Nullable Long id) {
        if (id == null)
            return null;

        return this.noteRepository.findById(id).orElse(null);
    }

    /**
     * Delete note with given id if not {@code null} (wont throw).
     * 
     * @param id of the note to delete
     */
    public void delete(@Nullable Long id) {
        if (id == null)
            return;

        this.noteRepository.deleteById(id);

        this.tagService.removeOrphanTags();
    }
}