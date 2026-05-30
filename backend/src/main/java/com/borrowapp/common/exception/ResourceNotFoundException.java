// com/borrowapp/common/exception/ResourceNotFoundException.java
package com.borrowapp.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}