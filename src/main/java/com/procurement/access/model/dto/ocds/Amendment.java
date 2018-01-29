
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "date",
    "rationale",
    "id",
    "description",
    "amendsReleaseID",
    "releaseID",
    "changes"
})
public class Amendment {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this amendment: often the amendment number")
    private final String id;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date of this amendment.")
    private final LocalDateTime date;

    @JsonProperty("description")
    @JsonPropertyDescription("A free text, or semi-structured, description of the changes made in this amendment.")
    private final String description;

    @JsonProperty("rationale")
    @JsonPropertyDescription("An explanation for the amendment.")
    @Pattern(regexp = "^(rationale_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})" +
        "(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String rationale;

    @JsonProperty("amendsReleaseID")
    @JsonPropertyDescription("Provide the identifier (release.id) of the OCDS release (from this contracting process)" +
        " that provides the values for this contracting process **before** the amendment was made.")
    private final String amendsReleaseID;

    @JsonProperty("releaseID")
    @JsonPropertyDescription("Provide the identifier (release.id) of the OCDS release (from this contracting process)" +
        " that provides the values for this contracting process **after** the amendment was made.")
    private final String releaseID;

    @JsonProperty("changes")
    @JsonPropertyDescription("An array change objects describing the fields changed, and their former values. " +
        "(Deprecated in 1.1)")
    @Valid
    private final List<Change> changes;

    @JsonCreator
    public Amendment(@JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
                     @JsonProperty("releaseID") final String releaseID,
                     @JsonProperty("id") final String id,
                     @JsonProperty("description") final String description,
                     @JsonProperty("amendsReleaseID") final String amendsReleaseID,
                     @JsonProperty("rationale") final String rationale,
                     @JsonProperty("changes") final List<Change> changes) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.rationale = rationale;
        this.amendsReleaseID = amendsReleaseID;
        this.releaseID = releaseID;
        this.changes = changes;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date)
                                    .append(releaseID)
                                    .append(id)
                                    .append(description)
                                    .append(amendsReleaseID)
                                    .append(rationale)
                                    .append(changes)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Amendment)) {
            return false;
        }
        final Amendment rhs = (Amendment) other;
        return new EqualsBuilder().append(date, rhs.date)
                                  .append(releaseID, rhs.releaseID)
                                  .append(id, rhs.id)
                                  .append(description, rhs.description)
                                  .append(amendsReleaseID, rhs.amendsReleaseID)
                                  .append(rationale, rhs.rationale)
                                  .append(changes, rhs.changes)
                                  .isEquals();
    }
}
