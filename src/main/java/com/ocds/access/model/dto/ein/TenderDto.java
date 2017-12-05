
package com.ocds.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "title",
    "description",
    "classification"
})
public class TenderDto {

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this tender. This will often be used by applications as a headline to " +
        "attract interest, and to help analysts understand the nature of this procurement.")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A summary description of the tender. This should complement structured information " +
        "provided using the items array. Descriptions should be short and easy to read. Avoid using ALL CAPS. ")
    @NotNull
    private final String description;

    @JsonProperty("classification")
    @Valid
    @NotNull
    private final ClassificationDto classification;

    @JsonCreator
    public TenderDto(@JsonProperty("title") final String title,
                     @JsonProperty("description") final String description,
                     @JsonProperty("classification") final ClassificationDto classification) {
        this.title = title;
        this.description = description;
        this.classification = classification;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(title)
                                    .append(description)
                                    .append(classification)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TenderDto)) {
            return false;
        }
        final TenderDto rhs = (TenderDto) other;
        return new EqualsBuilder().append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(classification, rhs.classification)
                                  .isEquals();
    }


}
