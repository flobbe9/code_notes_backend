package de.code_notes.backend.services;

import static de.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.abstracts.AbstractService;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.Note;
import de.code_notes.backend.repositories.NoteRepository;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;


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


    /**
     * @return
     * @throws ResponseStatusException
     */
    public List<Note> getAllByCurrentAppUser() throws ResponseStatusException {

        AppUser appUser = this.appUserService.getCurrent();

        return this.noteRepository.findAllByAppUserEmail(appUser.getEmail());
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

        AppUser currentAppUser = this.appUserService.getCurrent();

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
    public Note getById(@Nullable Long id) {

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