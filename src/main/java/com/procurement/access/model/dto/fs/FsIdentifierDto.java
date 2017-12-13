
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
    "scheme",
    "id",
    "legalName",
    "uri"
})
public class FsIdentifierDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier of the organization in the selected scheme.")
    @NotNull
    private final String id;

    @JsonProperty("scheme")
    @JsonPropertyDescription("Organization identifiers should be drawn from an existing organization identifier list." +
        " The scheme field is used to indicate the list or register from which the identifier is drawn. This value " +
        "should be drawn from the [Organization FsIdentifierDto Scheme](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#organization-identifier-scheme) codelist.")
    @NotNull
    private final String scheme;

    @JsonProperty("legalName")
    @JsonPropertyDescription("The legally registered name of the organization.")
    @NotNull
    private final String legalName;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI to identify the organization, such as those provided by [Open Corporates]" +
        "(http://www.opencorporates.com) or some other relevant URI provider. This is not for listing the website of " +
        "the organization: that can be done through the URL field of the Organization contact point.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String uri;

    @JsonCreator
    public FsIdentifierDto(@JsonProperty("scheme") final String scheme,
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
        if (!(other instanceof FsIdentifierDto)) {
            return false;
        }
        final FsIdentifierDto rhs = (FsIdentifierDto) other;
        return new EqualsBuilder().append(scheme, rhs.scheme)
                                  .append(id, rhs.id)
                                  .append(legalName, rhs.legalName)
                                  .append(uri, rhs.uri)
                                  .isEquals();
    }
}
