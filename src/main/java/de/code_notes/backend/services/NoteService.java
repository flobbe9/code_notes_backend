package de.code_notes.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.Note;
import de.code_notes.backend.repositories.NoteRepository;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private CodeBlockService codeBlockService;
    
    @Autowired
    private PlainTextBlockService plainTextBlockService;


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

        // get note from db
        Note oldNote = getById(note.getId());
        
        // case: note exists
        if (oldNote != null) {
            // delete old blocks
            this.codeBlockService.deleteAll(oldNote.getCodeBlocks());
            this.plainTextBlockService.deleteAll(oldNote.getPlainTextBlocks());
        }

        // save tags and update set
        note.setTags(this.tagService.saveOrGetNoteTags(note));

        note = this.noteRepository.save(note);

        this.tagService.removeOrphanTags();

        return note;
    }


    /**
     * @param id
     * @return note with given id or {@code null}
     */
    public Note getById(Long id) {

        if (id == null)
            return null;

        return this.noteRepository.findById(id).orElse(null);
    }


    /**
     * Delete given note if not {@code null} (wont throw).
     * 
     * @param note
     */
    public void delete(Note note) {

        if (note != null)
            this.noteRepository.delete(note);
    }


    /**
     * Delete note with given id if not {@code null} (wont throw).
     * 
     * @param id
     */
    public void delete(Long id) {

        this.delete(getById(id));
    }
}