package de.code_notes.backend.services;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.entities.Note;
import de.code_notes.backend.repositories.NoteRepository;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private CodeBlockService codeBlockService;
    
    @Autowired
    private PlainTextBlockService plainTextBlockService;

    @Autowired
    private Validator validator;


    /**
     * @param appUserEmail email of {@code appUser}
     * @return all notes related to {@code appUser} with given email or empty list
     */
    // TODO: test this
    public List<Note> getAllByUser(String appUserEmail) {

        return this.noteRepository.findAllByAppUserEmail(appUserEmail);
    }


    /**
     * TODO
     * @param note
     * @return
     */
    public Note save(Note note) {

        // case: falsy param
        if (note == null)
            throw new IllegalStateException("Failed to save note. 'note' cannot be null");

        // validate
        validateAndThrow(note);

        // case: appUser does not exist
        if (!this.appUserService.exists(note.getAppUser()))
            throw new ResponseStatusException(CONFLICT, "Failed to save note. 'note.appUser' does not exist");

        // case: note exists
        Note oldNote = getById(note.getId());
        // TODO: test
        if (oldNote != null) {
            // delete old blocks
            this.codeBlockService.deleteAll(oldNote.getCodeBlocks());
            this.plainTextBlockService.deleteAll(oldNote.getPlainTextBlocks());
        }

        // save and get tags
        if (note.getTags() != null)
            note.setTags(this.tagService.saveOrGetNoteTags(note));

        note = this.noteRepository.save(note);

        // remove tags that are no longer related to any note
        // TODO: test
        this.tagService.removeOrphanTags(note.getAppUser());

        return note;
    }


    /**
     * @param id
     * @return note with given id or {@code null}
     */
    public Note getById(@Nullable Long id) {

        if (id == null)
            return null;

        return this.noteRepository.findById(id).orElse(null);
    }


    /**
     * Delete given note if not {@code null} (wont throw).
     * 
     * @param note
     */
    public void delete(@Nullable Note note) {

        if (note != null)
            this.noteRepository.delete(note);
    }


    /**
     * Delete note with given id if not {@code null} (wont throw).
     * 
     * @param id
     */
    public void delete(@Nullable Long id) {

        this.delete(getById(id));
    }


    // TODO: put this inside abstract service
    private boolean validateAndThrow(Note note) {

        if (note == null)
            throw new IllegalStateException("Failed to validate note. 'note' cannot be null");

        this.validator.validateObject(note)
                      .failOnError((message) -> new ResponseStatusException(BAD_REQUEST, "'note' invalid"));

        return true;
    }
}