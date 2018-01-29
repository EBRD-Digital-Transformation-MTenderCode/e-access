
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "scheme",
    "id",
    "legalName",
    "uri"
})
public class Identifier {
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier of the organization in the selected scheme.")
    private final String id;

    @JsonProperty("scheme")
    @JsonPropertyDescription("Organization identifiers should be drawn from an existing organization identifier list." +
        " The scheme field is used to indicate the list or register from which the identifier is drawn. This value " +
        "should be drawn from the [Organization Identifier Scheme](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#organization-identifier-scheme) codelist.")
    private final String scheme;

    @JsonProperty("legalName")
    @JsonPropertyDescription("The legally registered name of the organization.")
    @Pattern(regexp = "^(legalName_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})" +
        "(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String legalName;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI to identify the organization, such as those provided by [Open Corporates]" +
        "(http://www.opencorporates.com) or some other relevant URI provider. This is not for listing the website of " +
        "the organization: that can be done through the URL field of the Organization contact point.")
    private final URI uri;

    @JsonCreator
    public Identifier(@JsonProperty("scheme") final String scheme,
                      @JsonProperty("id") final String id,
                      @JsonProperty("legalName") final String legalName,
                      @JsonProperty("uri") final URI uri) {
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
