package de.code_notes.backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.code_notes.backend.abstracts.AbstractService;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.Note;
import de.code_notes.backend.repositories.NoteRepository;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class NoteService extends AbstractService<Note> {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private NoteInputService noteInputService;


    /**
     * @param appUser to find notes for
     * @return all notes related to {@code appUser} or empty list
     */
    public List<Note> getAllByAppUser(AppUser appUser) {

        return this.noteRepository.findAllByAppUser(appUser);
    }


    /**
     * Save or create given {@code note} and reference it to given {@code appUser}.
     * Also save or delete tags if necessary.
     * 
     * @param note to save. {@code appUser} field might not be present because of {@code @JsonIgnore}
     * @return saved {@code note}
     * @throws IllegalArgumentException if a param is {@code null}
     */
    public Note save(Note note, AppUser appUser) {

        // case: falsy params
        if (note == null || appUser == null)
            throw new IllegalArgumentException("Failed to save note. 'note' or 'appUser' are null");

        // validate
        super.validateAndThrow(note);

        // save related entites
        note = saveRelatedEntities(note, appUser);

        // save
        note = this.noteRepository.save(note);

        // remove tags that are no longer related to any note
        this.tagService.removeOrphanTags(appUser);

        return note;
    }


    /**
     * Save the notes related entities. If the {@code note} does not exist in db yet, save it beforehand. Wont save the {@code note} after updating relations.
     * <p>
     * The note's existing inputs will be deleted and then saved as new entity.
     * 
     * @param note to save the relations from
     * @param appUser for the {@code note} to reference
     * @return the {@code note} with the updated
     * @throws IllegalArgumentException if a param is {@code null}
     */
    private Note saveRelatedEntities(Note note, AppUser appUser) {

        // case: falsy params
        if (note == null || appUser == null)
            throw new IllegalArgumentException("Failed to save related entities of note. 'note' or 'appUser' are null");

        // set app user since they're ignored in the note object
        note.setAppUser(appUser);

        Note oldNote = getById(note.getId());
        
        this.tagService.handleSaveNote(note, appUser);

        // case: note exists
        if (oldNote != null) {
            // delete old inputs
            this.noteInputService.deleteAll(oldNote.getNoteInputs());

        // case: note does not exist
        } else 
            note = this.noteRepository.save(note);

        this.noteInputService.addNoteReferences(note);

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
}