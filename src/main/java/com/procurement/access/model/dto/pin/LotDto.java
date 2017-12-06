
package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
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
    "value",
    "options",
    "renewals",
    "variants"
})
public class LotDto {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this lot, such as a lot number. This is used in relatedLot " +
        "references at the item, document and award level.")
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this lot.")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A description of this lot.")
    @NotNull
    private final String description;

    @JsonProperty("value")
    @Valid
    @NotNull
    private final ValueDto value;

    @JsonProperty("options")
    @JsonPropertyDescription("Details about lot options: if they will be accepted and what they can consist of. " +
        "Required by the EU")
    @Valid
    @NotNull
    private final List<OptionDto> options;

    @JsonProperty("renewals")
    @JsonPropertyDescription("Details of allowable contract renewals")
    @Valid
    @NotNull
    private final List<RenewalDto> renewals;

    @JsonProperty("variants")
    @JsonPropertyDescription("Details about lot variants: if they will be accepted and what they can consist of. " +
        "Required by the EU")
    @Valid
    @NotNull
    private final List<VariantDto> variants;

    @JsonCreator
    public LotDto(@JsonProperty("id") final String id,
                  @JsonProperty("title") final String title,
                  @JsonProperty("description") final String description,
                  @JsonProperty("value") final ValueDto value,
                  @JsonProperty("options") final List<OptionDto> options,
                  @JsonProperty("renewals") final List<RenewalDto> renewals,
                  @JsonProperty("variants") final List<VariantDto> variants) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.value = value;
        this.options = options;
        this.renewals = renewals;
        this.variants = variants;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(value)
                                    .append(options)
                                    .append(renewals)
                                    .append(variants)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LotDto)) {
            return false;
        }
        final LotDto rhs = (LotDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(value, rhs.value)
                                  .append(options, rhs.options)
                                  .append(renewals, rhs.renewals)
                                  .append(variants, rhs.variants)
                                  .isEquals();
    }
}
