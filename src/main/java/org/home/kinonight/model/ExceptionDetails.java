package org.home.kinonight.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class ExceptionDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = -5828959004473736022L;

    private long chatId;
    private String message;
}
