package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "id",
    "title",
    "description",
    "classification"
})
public class EinTenderDto {

    @NotNull
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for the tender.")
    private final String id;

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
    private final EinClassificationDto classification;

    @JsonCreator
    public EinTenderDto(@JsonProperty("id") final String id,
                        @JsonProperty("title") final String title,
                        @JsonProperty("description") final String description,
                        @JsonProperty("classification") final EinClassificationDto classification) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.classification = classification;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(classification)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EinTenderDto)) {
            return false;
        }
        final EinTenderDto rhs = (EinTenderDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(classification, rhs.classification)
                                  .isEquals();
    }


}
