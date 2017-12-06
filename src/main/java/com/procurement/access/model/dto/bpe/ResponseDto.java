package com.procurement.access.model.dto.bpe;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class ResponseDto<T> {

    @JsonProperty(value = "success")
    private Boolean success;

    @JsonProperty(value = "details")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ResponseDetailsDto> details;

    @JsonProperty(value = "data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public ResponseDto(@JsonProperty("success") final Boolean success,
                       @JsonProperty("details") final List<ResponseDetailsDto> details,
                       @JsonProperty("data") final T data) {
        this.success = success;
        this.details = details;
        this.data = data;
    }

    @Getter
    public static class ResponseDetailsDto {
        @JsonProperty("code")
        private String code;
        @JsonProperty("message")
        private String message;

        public ResponseDetailsDto(@JsonProperty("code")final String code, @JsonProperty("message")final String message) {
            this.code = code;
            this.message = message;
        }
    }
}
