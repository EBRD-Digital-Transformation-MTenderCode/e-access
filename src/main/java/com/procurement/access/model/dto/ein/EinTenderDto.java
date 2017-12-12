package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonPropertyOrder({
    "id",
    "title",
    "description",
    "classification",
    "status",
    "statusDetails"
})
public class EinTenderDto {

    @NotNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @NotNull
    private final String description;

    @JsonProperty("classification")
    @Valid
    @NotNull
    private final EinClassificationDto classification;

    @JsonProperty("status")
    private final EinTenderStatusDto status;

    @JsonProperty("statusDetails")
    private final EinTenderStatusDetailsDto statusDetails;

    @JsonCreator
    public EinTenderDto(@JsonProperty("id") final String id,
                        @JsonProperty("title") final String title,
                        @JsonProperty("description") final String description,
                        @JsonProperty("classification") final EinClassificationDto classification,
                        @JsonProperty("status") final EinTenderStatusDto status,
                        @JsonProperty("statusDetails") final EinTenderStatusDetailsDto statusDetails) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.classification = classification;
        this.status = status;
        this.statusDetails = statusDetails;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(classification)
                                    .append(status)
                                    .append(statusDetails)
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
                                  .append(status, rhs.status)
                                  .append(statusDetails, rhs.statusDetails)
                                  .isEquals();
    }
}
