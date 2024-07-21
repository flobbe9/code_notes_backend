package de.code_notes.backend.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.Note;
import de.code_notes.backend.entities.Tag;
import de.code_notes.backend.repositories.TagRepository;


/**
 * @since 0.0.1
 */
@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;


    /**
     * TODO
     * @param note
     * @return
     */
    public Set<Tag> saveOrGetNoteTags(Note note) {

        Set<Tag> tags = note.getTags()
                            .stream()
                            .map(tag -> {
                                // TODO: find by name and user
                                Tag tagFromDb = this.tagRepository.findByName(tag.getName()).orElse(null);

                                // case: new tag
                                if (tagFromDb == null) {
                                    // TODO: set user (get from session?)
                                    return this.tagRepository.save(tag);
                                    
                                // case: tag exists
                                } else 
                                    return tagFromDb;
                            })
                            .collect(Collectors.toSet());

        return tags;
    }


    /**
     * TODO
     * @return
     */
    public List<Tag> getAllByUser() {
        
        // TODO: by user
        return this.tagRepository.findAll();
    }


    // TODO
    public void removeOrphanTags() {

        // iterate all tags of user
            // if has no notes
                // delete
    }
}