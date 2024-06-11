package org.home.kinonight.exception;

import org.home.kinonight.model.ExceptionDetails;

public class WrongNameException extends BaseException {
    public WrongNameException(ExceptionDetails exceptionDetails) {
        super(exceptionDetails);
    }
}
