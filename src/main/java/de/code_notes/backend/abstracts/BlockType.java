package de.code_notes.backend.abstracts;


/**
 * Enum defining the type a block can have.
 * 
 * @since 0.0.1
 */
public enum BlockType {
    
    /** Contains simple plain text as value. */
    PLAIN_TEXT,

    /** Contains highlighted text as value depending on the programming language. */
    CODE,

    /** Contains highlighted text as value depending on the programming language. May also contain variables (does not make any difference for the backend). */
    CODE_WITH_VARIABLES;
}