package org.home.kinonight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scope {
    private String type;
    @JsonProperty("chat_id")
    private String chatId;
}
