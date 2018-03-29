package com.procurement.access.model.dto.bpe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ResponseDetailsDto {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    public ResponseDetailsDto(@JsonProperty("code") final String code,
                              @JsonProperty("message") final String message) {
        this.code = code;
        this.message = message;
    }
}
