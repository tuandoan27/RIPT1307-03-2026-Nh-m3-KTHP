// com/borrowapp/common/exception/ForbiddenException.java
package com.borrowapp.common.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}