package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "scheme",
        "legalName",
        "uri"
})
public class Identifier {

    @NotNull
    @JsonProperty("id")
    private final String id;

    @NotNull
    @JsonProperty("scheme")
    private final String scheme;

    @NotNull
    @JsonProperty("legalName")
    private final String legalName;

    @NotNull
    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public Identifier(@JsonProperty("scheme") final String scheme,
                      @JsonProperty("id") final String id,
                      @JsonProperty("legalName") final String legalName,
                      @JsonProperty("uri") final String uri) {
        this.id = id;
        this.scheme = scheme;
        this.legalName = legalName;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(scheme)
                .append(id)
                .append(legalName)
                .append(uri)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Identifier)) {
            return false;
        }
        final Identifier rhs = (Identifier) other;
        return new EqualsBuilder().append(scheme, rhs.scheme)
                .append(id, rhs.id)
                .append(legalName, rhs.legalName)
                .append(uri, rhs.uri)
                .isEquals();
    }
}
