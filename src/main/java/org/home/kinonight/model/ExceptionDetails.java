package org.home.kinonight.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionDetails {

    private long chatId;
    private String message;
}
