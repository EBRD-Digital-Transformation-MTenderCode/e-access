
package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "name",
    "email",
    "telephone",
    "faxNumber",
    "url"
})
public class FsContactPointDto {
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the contact person, department, or contact point, for correspondence " +
        "relating to this contracting process.")
    @NotNull
    private final String name;

    @JsonProperty("email")
    @JsonPropertyDescription("The e-mail address of the contact point/person.")
    @NotNull
    private final String email;

    @JsonProperty("telephone")
    @JsonPropertyDescription("The telephone number of the contact point/person. This should include the international" +
        " dialing code.")
    @NotNull
    private final String telephone;

    @JsonProperty("faxNumber")
    @JsonPropertyDescription("The fax number of the contact point/person. This should include the international " +
        "dialing code.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String faxNumber;

    @JsonProperty("url")
    @JsonPropertyDescription("A web address for the contact point/person.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String url;

    @JsonCreator
    public FsContactPointDto(@JsonProperty("name") final String name,
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
        if (!(other instanceof FsContactPointDto)) {
            return false;
        }
        final FsContactPointDto rhs = (FsContactPointDto) other;
        return new EqualsBuilder().append(name, rhs.name)
                                  .append(email, rhs.email)
                                  .append(telephone, rhs.telephone)
                                  .append(faxNumber, rhs.faxNumber)
                                  .append(url, rhs.url)
                                  .isEquals();
    }
}
