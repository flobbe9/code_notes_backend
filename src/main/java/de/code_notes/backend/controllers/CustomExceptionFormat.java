package de.code_notes.backend.controllers;

import java.time.LocalDateTime;


import de.code_notes.backend.helpers.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Class defining the exception format this api returns when catching any Exception.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomExceptionFormat {

    private String timestamp;

    private int status;

    private String message;

    private String path;

    
    public CustomExceptionFormat(int status, String message) {

        this.timestamp = Utils.formatLocalDateTimeDefault(LocalDateTime.now());
        this.status = status;
        this.message = message;
        this.path = Utils.getReqeustPath();             
    }
}