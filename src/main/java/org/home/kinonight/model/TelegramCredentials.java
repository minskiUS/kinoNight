package org.home.kinonight.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramCredentials {

    @JsonProperty("telegram_secret")
    private String secret;

    @JsonProperty("telegram_username")
    private String userName;
}
