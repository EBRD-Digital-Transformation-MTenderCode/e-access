package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private final String id;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime date;

    @JsonProperty("description")
    @JsonPropertyDescription("A free text, or semi-structured, description of the changes made in this amendment.")
    private final String description;

    @JsonProperty("rationale")
    private final String rationale;

    @JsonProperty("amendsReleaseID")
    private final String amendsReleaseID;

    @JsonProperty("releaseID")
    private final String releaseID;

    @JsonProperty("changes")
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
