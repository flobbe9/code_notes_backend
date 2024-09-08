package de.code_notes.backend.services;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.code_notes.backend.abstracts.AbstractService;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.Note;
import de.code_notes.backend.entities.Tag;
import de.code_notes.backend.repositories.TagRepository;
import jakarta.annotation.Nullable;


/**
 * @since 0.0.1
 */
@Service
public class TagService extends AbstractService<Tag> {
    
    @Autowired
    private TagRepository tagRepository;


    /**
     * Create given {@code note}'s tags or retrieve them from db if they already exist for this appUser. Then update {@code note.tags} (but dont save {@code note}).
     * 
     * @param note to update the tags and tag's list for
     * @param appUser referenced by the {@code note}
     * @throws IllegalArgumentException if {@code note} or {@code appUser} is {@code null}
     */
    public void handleSaveNote(Note note, AppUser appUser) {
        
        // case: falsy param
        if (note == null || appUser == null)
            throw new IllegalArgumentException("Failed to handle saving a note. 'note' or 'appUser' are null");
        
        // contains only tags from db
        Set<Tag> tags = getOrCreateNoteTags(note, appUser);

        if (tags != null)
            note.setTags(tags);
    }


    /**
     * Find every tag from given {@code note} in db and either save it if it didn't exist or replace the tag in the list with the
     * tag from the db.
     * 
     * @param note to update tags for
     * @return updated {@code note} tags or {@code null} if {@code note.tags} is {@code null}
     * @throws IllegalArgumentException if {@code note} or {@code appUser} is {@code null}
     */
    private Set<Tag> getOrCreateNoteTags(Note note, AppUser appUser) {

        // case: falsy param
        if (note == null || appUser == null)
            throw new IllegalArgumentException("Failed to save or get note tags. 'note' or 'appUser' are null");

        // case: has no tags
        if (note.getTags() == null)
            return null;
            
        // map tags from db
        return note.getTags()
                    .stream()
                    .map(tag -> {
                        Tag tagFromDb = this.tagRepository.findByNameAndAppUser(tag.getName(), appUser)
                                                            .orElse(null);

                        // case: new tag
                        if (tagFromDb == null) {
                            // validate
                            super.validateAndThrow(tag);
                            
                            tag.setAppUser(appUser);
                            return this.tagRepository.save(tag);
                            
                        // case: tag exists
                        } else 
                            return tagFromDb;
                    })
                    .collect(Collectors.toSet());
    }


    /**
     * @param appUser to get the tags for
     * @return list of all tags in db related to given {@code appUser} or an empty list
     */
    public List<Tag> getAllByUser(@Nullable AppUser appUser) {

        if (appUser == null)
            return new ArrayList<>();

        return this.tagRepository.findAllByAppUser(appUser);
    }


    /**
     * Remove all tags of given appUser that don't have any notes (which means that the tag is useless).
     * 
     * @param appUser to check the tags for
     */
    public void removeOrphanTags(@Nullable AppUser appUser) {

        // case: falsy param
        if (appUser == null)
            return;

        getAllByUser(appUser).forEach(tag -> {
            // case: tag has not notes anymore
            if (tag.getNotes().isEmpty())
                this.tagRepository.delete(tag);
        });
    }
}