package com.papel.imdb_clone.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//FileParsingException class
public class FileParsingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(FileParsingException.class);

    public FileParsingException(String message) {
        super(message);
        logger.error("File parsing error: {}", message);
    }
}
