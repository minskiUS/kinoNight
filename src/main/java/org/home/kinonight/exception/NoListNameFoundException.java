package org.home.kinonight.exception;

import org.home.kinonight.model.ExceptionDetails;

public class NoListNameFoundException extends BaseException {
    public NoListNameFoundException(ExceptionDetails exceptionDetails) {
        super(exceptionDetails);
    }
}
