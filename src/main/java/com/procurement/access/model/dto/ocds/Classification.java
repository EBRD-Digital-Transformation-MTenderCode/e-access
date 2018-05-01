package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "description",
        "scheme",
        "uri"
})
public class Classification {

    @NotNull
    @JsonProperty("id")
    private final String id;

    @NotNull
    @JsonProperty("description")
    private final String description;

    @NotNull
    @JsonProperty("scheme")
    private final Scheme scheme;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public Classification(@JsonProperty("scheme") final Scheme scheme,
                          @JsonProperty("id") final String id,
                          @JsonProperty("description") final String description,
                          @JsonProperty("uri") final String uri) {
        this.id = id;
        this.description = description;
        this.scheme = scheme;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(scheme)
                .append(id)
                .append(description)
                .append(uri)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Classification)) {
            return false;
        }
        final Classification rhs = (Classification) other;
        return new EqualsBuilder().append(scheme, rhs.scheme)
                .append(id, rhs.id)
                .append(description, rhs.description)
                .append(uri, rhs.uri)
                .isEquals();
    }
}
