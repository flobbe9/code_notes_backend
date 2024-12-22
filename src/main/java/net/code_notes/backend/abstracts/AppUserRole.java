package net.code_notes.backend.abstracts;


/**
 * Enum defining roles a {@code User} entity can have.
 * 
 * @since 0.0.1
 */
public enum AppUserRole {

    USER,

    ADMIN;


    /**
     * Returns the name of the user role sothat spring security would recognize it as role.
     * 
     * @return {@code "ROLE_" + this.name()}
     */
    public String getSecurityName() {

        return "ROLE_" + this.name();
    }
}