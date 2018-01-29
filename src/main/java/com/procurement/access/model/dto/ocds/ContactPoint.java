package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "email",
        "telephone",
        "faxNumber",
        "url"
})
public class ContactPoint {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("email")
    private final String email;

    @JsonProperty("telephone")
    private final String telephone;

    @JsonProperty("faxNumber")
    private final String faxNumber;

    @JsonProperty("url")
    private final String url;

    @JsonCreator
    public ContactPoint(@JsonProperty("name") final String name,
                        @JsonProperty("email") final String email,
                        @JsonProperty("telephone") final String telephone,
                        @JsonProperty("faxNumber") final String faxNumber,
                        @JsonProperty("url") final String url) {
        this.name = name;
        this.email = email;
        this.telephone = telephone;
        this.faxNumber = faxNumber;
        this.url = url;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                .append(email)
                .append(telephone)
                .append(faxNumber)
                .append(url)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ContactPoint)) {
            return false;
        }
        final ContactPoint rhs = (ContactPoint) other;
        return new EqualsBuilder().append(name, rhs.name)
                .append(email, rhs.email)
                .append(telephone, rhs.telephone)
                .append(faxNumber, rhs.faxNumber)
                .append(url, rhs.url)
                .isEquals();
    }
}
