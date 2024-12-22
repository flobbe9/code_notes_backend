package net.code_notes.backend.services;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Nullable;
import net.code_notes.backend.abstracts.AbstractService;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.Note;
import net.code_notes.backend.entities.Tag;
import net.code_notes.backend.helpers.Utils;
import net.code_notes.backend.repositories.TagRepository;


/**
 * @since 0.0.1
 */
@Service
public class TagService extends AbstractService<Tag> {
    
    @Autowired
    private TagRepository tagRepository;

    @Autowired 
    private AppUserService appUserService;


    @Override
    public Tag save(Tag tag) {

        Utils.assertArgsNotNullAndNotBlankOrThrow(tag);

        AppUser appUser = this.appUserService.getCurrent();

        if (existsByNameAndAppUser(tag, appUser))
            return update(tag, appUser);

        return saveNew(tag, appUser);
    }


    /**
     * Create given {@code note}'s tags or retrieve them from db if they already exist for this appUser. Then update {@code note.tags} (but dont save {@code note}).
     * 
     * @param note to update the tags and tag's list for
     * @param appUser referenced by the {@code note}
     * @throws IllegalArgumentException if {@code note} or {@code appUser} is {@code null}
     */
    public void handleSaveNote(Note note, AppUser appUser) {
        
        Utils.assertArgsNotNullAndNotBlankOrThrow(note, appUser);
        
        // contains only tags from db
        List<Tag> tags = getOrCreateNoteTags(note, appUser);

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
    private List<Tag> getOrCreateNoteTags(Note note, AppUser appUser) {

        Utils.assertArgsNotNullAndNotBlankOrThrow(note, appUser);

        // case: has no tags
        if (note.getTags() == null)
            return null;
            
        // map tags from db
        return note.getTags()
                    .stream()
                    .map(tag -> {
                        Tag tagFromDb = this.tagRepository
                            .findByNameAndAppUser(tag.getName(), appUser)
                            .orElse(null);

                        // case: new tag
                        if (tagFromDb == null)
                            return saveNew(tag, appUser);
                            
                        // case: existing tag
                        else
                            return tagFromDb;
                    })
                    .toList();
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

        this.tagRepository.deleteByNotesIsNull();
    }
    

    /**
     * Overload. Use the tags of the currently logged in app user
     * @throws ResponseStatusException 401 if not logged in
     * @throws IllegalStateException if the logged in principal is not of a handled type
     */
    public void removeOrphanTags() throws ResponseStatusException, IllegalStateException {

        removeOrphanTags(this.appUserService.getCurrent());
    }


    /**
     * Uses current app user as backreference.
     * 
     * @throws ResponseStatusException 409 if tag does exist by name and app user
     */
    @Override
    protected Tag saveNew(Tag tag) throws ResponseStatusException, IllegalArgumentException {

        return saveNew(tag, this.appUserService.getCurrent());
    }
    

    /**
     * Overload
     * @param tag
     * @param appUser
     * @return
     * @throws ResponseStatusException 409 if tag does exist by name and app user
     * @throws IllegalArgumentException
     */
    protected Tag saveNew(Tag tag, AppUser appUser) throws ResponseStatusException, IllegalArgumentException {

        Utils.assertArgsNotNullAndNotBlankOrThrow(tag, appUser);

        validateAndThrow(tag);

        if (existsByNameAndAppUser(tag, appUser))
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Failed to save new tag. A tag with name '%s' does already exist for the current app user", tag.getName()));

        setIgnoredFields(tag, appUser);

        // make sure this tag is saved as new instance
        tag.setId(null);

        tag = this.tagRepository.save(tag);

        return tag;
    }


    /**
     * Uses current app user as backreference.
     * 
     * @throws ResponseStatusException 409 if tag does not exist by name and app user
     */
    @Override
    protected Tag update(Tag tag) throws ResponseStatusException, IllegalArgumentException {

        return update(tag, this.appUserService.getCurrent());
    }


    /**
     * Overload
     * @param tag
     * @param appUser
     * @return
     * @throws ResponseStatusException 409 if tag does not exist by name and app user
     * @throws IllegalArgumentException
     */
    protected Tag update(Tag tag, AppUser appUser) throws ResponseStatusException, IllegalArgumentException {

        Utils.assertArgsNotNullAndNotBlankOrThrow(tag, appUser);

        validateAndThrow(tag);

        Tag tagFromDb = this.tagRepository
            .findByNameAndAppUser(tag.getName(), appUser)
            .orElse(null);

        if (tagFromDb == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Failed to update tag. No tag with name '%s' exists for the current app user", tag.getName()));

        // make sure the existing instance is saved instead of a new one
        tag.copyAbstractEntityFields(tagFromDb);

        setIgnoredFields(tag, appUser);

        tag = this.tagRepository.save(tag);

        return tag;
    }

        
    /**
     * Set {@code @JsonIgnore} annotated fields, altering given {@code tag} instance.
     * 
     * @param tag
     * @param appUser to set to {@code tag.appUser}
     * @throws IllegalArgumentException
     */
    private void setIgnoredFields(Tag tag, AppUser appUser) throws IllegalArgumentException {

        Utils.assertArgsNotNullAndNotBlankOrThrow(tag, appUser);

        tag.setAppUser(appUser);
    }


    /**
     * Uses the app users's {@code id} as comparison criteria.
     * 
     * @param tag
     * @param appUser 
     * @return
     */
    public boolean existsByNameAndAppUser(Tag tag, AppUser appUser) {

        Utils.assertArgsNotNullAndNotBlankOrThrow(tag, appUser);

        return this.tagRepository.existsByNameAndAppUser(tag.getName(), appUser);
    }
}