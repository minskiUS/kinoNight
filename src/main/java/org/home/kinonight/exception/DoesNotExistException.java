package org.home.kinonight.exception;

import org.home.kinonight.model.ExceptionDetails;

public class DoesNotExistException extends BaseException {
    public DoesNotExistException(ExceptionDetails exceptionDetails) {
        super(exceptionDetails);
    }
}
