package com.papel.imdb_clone.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when a duplicate entry is detected in the system.
 * Can be used for any type of duplicate entry (username, email, content, etc.).
 */
public class DuplicateEntryException extends InvalidEntityException {
    private final String fieldName;
    private final Object fieldValue;


    /**
     * Creates a new DuplicateEntryException with entity and field details.
     */
    public DuplicateEntryException(String entityName, Object entityId, String fieldName, Object fieldValue) {
        super(entityName, entityId,
                String.format("Duplicate entry found for %s: %s", fieldName, fieldValue),
                createFieldErrors(fieldName, fieldValue), null);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }


    //create field errors
    private static Map<String, List<String>> createFieldErrors(String fieldName, Object fieldValue) {
        //create field errors
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(fieldName, Collections.singletonList("already exists: " + fieldValue));
        return errors;
    }

}
