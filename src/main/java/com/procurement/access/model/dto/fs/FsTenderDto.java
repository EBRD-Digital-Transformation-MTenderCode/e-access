package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
        "status"
})
public class FsTenderDto {
    @NotNull
    @JsonProperty("id")
    private String id;
    @JsonProperty("title")
    private final String title;
    @JsonProperty("description")
    private final String description;
    @JsonProperty("status")
    private FsTenderStatusDto status;

    @JsonCreator
    public FsTenderDto(@JsonProperty("id") final String id,
                       @JsonProperty("title") final String title,
                       @JsonProperty("description") final String description,
                       @JsonProperty("status") final FsTenderStatusDto status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(title)
                .append(description)
                .append(status)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FsTenderDto)) {
            return false;
        }
        final FsTenderDto rhs = (FsTenderDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(status, rhs.status)
                .isEquals();
    }
}
