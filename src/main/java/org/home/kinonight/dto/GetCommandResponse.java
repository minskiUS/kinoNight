package org.home.kinonight.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetCommandResponse {
    private boolean ok;
    private List<Command> result;
}
