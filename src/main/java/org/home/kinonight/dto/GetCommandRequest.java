package org.home.kinonight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCommandRequest {

    private Scope scope;
}
