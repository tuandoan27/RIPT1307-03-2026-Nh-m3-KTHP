// com/borrowapp/common/exception/BadRequestException.java
package com.borrowapp.common.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}