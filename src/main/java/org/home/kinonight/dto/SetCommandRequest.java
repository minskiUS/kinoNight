package org.home.kinonight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetCommandRequest {
    private List<Command> commands;
    private Scope scope;
}
