package de.code_notes.backend.controllers;


/**
 * Record defining the exception format this api returns when catching any Exception.
 * 
 * @since 0.0.1
 */
public record CustomExceptionFormat(

    String timestamp,
    int status,
    String message,
    String path
) {}