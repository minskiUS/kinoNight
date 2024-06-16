package org.home.kinonight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetCommandRequest {
    @JsonProperty("commands")
    private List<CommandRequest> commandRequests;
    private Scope scope;
}
