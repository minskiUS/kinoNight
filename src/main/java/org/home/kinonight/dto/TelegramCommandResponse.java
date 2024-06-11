package org.home.kinonight.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TelegramCommandResponse {

    private boolean ok;
    private boolean result;
}
