package net.code_notes.backend.services;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.abstracts.AbstractService;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.Note;
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
    private Oauth2Service oauth2Service;


    /**
     * @return all notes of the app user currently logged in
     * @throws ResponseStatusException
     * @deprecated use {@link #getByCurrentAppUserOrderByCreatedDescPageable} instead
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public List<Note> getAllByCurrentAppUser() throws ResponseStatusException {
        AppUser appUser = this.appUserService.getCurrent();

        if (this.oauth2Service.isOauth2Session())
            return this.noteRepository.findAllByAppUserOauth2IdOrderByCreatedDesc(appUser.getOauth2Id());

        return this.noteRepository.findAllByAppUserEmailOrderByCreatedDesc(appUser.getEmail());
    }

    public long countByCurrentAppUser() {
        AppUser currentAppUser = this.appUserService.getCurrent();

        if (this.oauth2Service.isOauth2Session())
            return this.noteRepository.countByAppUserOauth2Id(currentAppUser.getOauth2Id());

        return this.noteRepository.countByAppUserEmail(currentAppUser.getEmail());
    }

    /**
     * @param pageNumber 0-based
     * @param pageSize the number of notes per page. Min 1
     * @return a page of notes related to the current app user
     * @throws ResponseStatusException
     */
    // TODO
        // add params
    public List<Note> getByCurrentAppUserOrderByCreatedDescPageable(int pageNumber, int pageSize) throws ResponseStatusException {
        AppUser appUser = this.appUserService.getCurrent();

        if (this.oauth2Service.isOauth2Session())
            return this.noteRepository.findByAppUserOauth2IdOrderByCreatedDesc(appUser.getOauth2Id(), PageRequest.of(pageNumber, pageSize));

        // search and consider
            // oauth2
            // pagenum
            // pagesize
            // searchphrase
            // searchtags

        return this.noteRepository.findByAppUserEmailOrderByCreatedDesc(appUser.getEmail(), PageRequest.of(pageNumber, pageSize));
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