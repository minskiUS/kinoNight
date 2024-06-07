package org.home.kinonight.feign;

import org.home.kinonight.config.FeignConfig;
import org.home.kinonight.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "TelegramClient",
        url = "${telegram.apiUrl}",
        configuration = FeignConfig.class)
public interface TelegramClient {
    @PostMapping(path = "/setMyCommands")
    TelegramCommandResponse setCommand(@RequestBody SetCommandRequest setCommandRequest);

    @PostMapping(path = "/deleteMyCommands")
    TelegramCommandResponse deleteCommand(@RequestBody DeleteCommandRequest deleteCommandRequest);

    @PostMapping(path = "/getMyCommands")
    GetCommandResponse getCommand(@RequestBody GetCommandRequest getCommandRequest);
}
