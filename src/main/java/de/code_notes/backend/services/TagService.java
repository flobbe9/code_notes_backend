package de.code_notes.backend.services;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.Note;
import de.code_notes.backend.entities.Tag;
import de.code_notes.backend.repositories.TagRepository;
import jakarta.annotation.Nullable;


/**
 * @since 0.0.1
 */
@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;


    /**
     * Find every tag from given {@code note} in db and either save it if it didn't exist or replace the tag in the list with the
     * tag from the db.
     * 
     * @param note to update tags for
     * @return updated {@code note} tags
     * @throws IllegalStateException if {@code note} or {@code note.appUser} is {@code null}
     */
    public Set<Tag> saveOrGetNoteTags(Note note) {

        // case: falsy param
        if (note == null || note.getAppUser() == null)
            throw new IllegalStateException("Failed to save or get note tags. 'note' or 'note.appUser' are null");
            
        AppUser appUser = note.getAppUser();

        Set<Tag> tags = note.getTags()
                            .stream()
                            .map(tag -> {
                                Tag tagFromDb = this.tagRepository.findByNameAndAppUser(tag.getName(), appUser).orElse(null);

                                // case: new tag
                                if (tagFromDb == null) {
                                    tag.setAppUser(appUser);
                                    return this.tagRepository.save(tag);
                                    
                                // case: tag exists
                                } else 
                                    return tagFromDb;
                            })
                            .collect(Collectors.toSet());

        return tags;
    }


    /**
     * @param appUser to get the tags for
     * @return list of all tags in db related to given {@code appUser} or an empty list
     */
    // TODO: test
        // is this even necessary?
    public List<Tag> getAllByUser(@Nullable AppUser appUser) {

        if (appUser == null)
            return new ArrayList<>();

        return this.tagRepository.findAllByAppUser(appUser);
    }


    // TODO
    public void removeOrphanTags(AppUser appUser) {

        // iterate all tags of user
            // if has no notes
                // delete
    }
}