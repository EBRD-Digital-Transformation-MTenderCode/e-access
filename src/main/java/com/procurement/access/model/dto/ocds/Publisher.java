package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "scheme",
        "uid",
        "uri"
})
public class Publisher {
    @JsonProperty("name")
    @NotNull
    private final String name;

    @JsonProperty("scheme")
    private final String scheme;

    @JsonProperty("uid")
    private final String uid;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public Publisher(@JsonProperty("name") final String name,
                     @JsonProperty("scheme") final String scheme,
                     @JsonProperty("uid") final String uid,
                     @JsonProperty("uri") final String uri) {
        this.name = name;
        this.scheme = scheme;
        this.uid = uid;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                .append(scheme)
                .append(uid)
                .append(uri)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Publisher)) {
            return false;
        }
        final Publisher rhs = (Publisher) other;
        return new EqualsBuilder().append(name, rhs.name)
                .append(scheme, rhs.scheme)
                .append(uid, rhs.uid)
                .append(uri, rhs.uri)
                .isEquals();
    }
}
