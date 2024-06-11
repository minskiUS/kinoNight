package org.home.kinonight.exception;

import lombok.Getter;
import org.home.kinonight.model.ExceptionDetails;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class BaseException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7157384938672608025L;

    private final ExceptionDetails exceptionDetails;

    public BaseException(ExceptionDetails exceptionDetails) {
        super(exceptionDetails.getMessage());
        this.exceptionDetails = exceptionDetails;
    }
}
