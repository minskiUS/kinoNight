package org.home.kinonight.exception;

import org.home.kinonight.model.ExceptionDetails;

public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(ExceptionDetails exceptionDetails) {
        super(exceptionDetails);
    }
}
