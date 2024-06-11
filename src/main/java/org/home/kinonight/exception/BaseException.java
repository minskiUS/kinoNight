package org.home.kinonight.exception;

import lombok.Getter;
import org.home.kinonight.model.ExceptionDetails;

@Getter
public class BaseException extends RuntimeException {

    private final ExceptionDetails exceptionDetails;

    public BaseException(ExceptionDetails exceptionDetails) {
        super(exceptionDetails.getMessage());
        this.exceptionDetails = exceptionDetails;
    }
}
