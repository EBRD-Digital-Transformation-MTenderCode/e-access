
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "projectIdentifier",
    "projectName",
    "uri"
})
public class EuropeanUnionFunding {
    @JsonProperty("projectIdentifier")
    @JsonPropertyDescription("National identifier of the EU project providing partial or full funding")
    private final String projectIdentifier;

    @JsonProperty("projectName")
    @JsonPropertyDescription("Name or other national identification of the project providing full or partial funding.")
    private final String projectName;

    @JsonProperty("uri")
    @JsonPropertyDescription("Uri of the project providing full or partial funding.")
    private final URI uri;

    @JsonCreator
    public EuropeanUnionFunding(@JsonProperty("projectIdentifier") final String projectIdentifier,
                                @JsonProperty("projectName") final String projectName,
                                @JsonProperty("uri") final URI uri) {
        this.projectIdentifier = projectIdentifier;
        this.projectName = projectName;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(projectIdentifier)
                                    .append(projectName)
                                    .append(uri)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EuropeanUnionFunding)) {
            return false;
        }
        final EuropeanUnionFunding rhs = (EuropeanUnionFunding) other;
        return new EqualsBuilder().append(projectIdentifier, rhs.projectIdentifier)
                                  .append(projectName, rhs.projectName)
                                  .append(uri, rhs.uri)
                                  .isEquals();
    }
}
