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
     * @deprecated Use the builder pattern or constructor with field details
     */
    @Deprecated
    public DuplicateEntryException(String message) {
        super(null);
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * @deprecated Use the builder pattern instead
     */
    @Deprecated
    public DuplicateEntryException(String fieldName, Object fieldValue) {
        this(null, null, fieldName, fieldValue);
    }

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

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }


    private static Map<String, List<String>> createFieldErrors(String fieldName, Object fieldValue) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(fieldName, Collections.singletonList("already exists: " + fieldValue));
        return errors;
    }

    /**
     * Builder for DuplicateEntryException.
     */
    public static class Builder {
        private String entityName;
        private Object entityId;
        private String fieldName;
        private Object fieldValue;
        private String customMessage;

        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder entityId(Object entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder field(String fieldName, Object fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            return this;
        }

        public Builder message(String message) {
            this.customMessage = message;
            return this;
        }

        public DuplicateEntryException build() {
            return new DuplicateEntryException(
                    entityName,
                    entityId,
                    fieldName,
                    fieldValue
            );
        }
    }
}
