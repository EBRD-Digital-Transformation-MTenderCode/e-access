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
        "projectIdentifier",
        "projectName",
        "uri"
})
public class EuropeanUnionFunding {

    @NotNull
    @JsonProperty("projectIdentifier")
    private final String projectIdentifier;

    @NotNull
    @JsonProperty("projectName")
    private final String projectName;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public EuropeanUnionFunding(@JsonProperty("projectIdentifier") final String projectIdentifier,
                                @JsonProperty("projectName") final String projectName,
                                @JsonProperty("uri") final String uri) {
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
