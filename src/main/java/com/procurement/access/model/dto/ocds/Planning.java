
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "budget",
    "rationale",
    "documents",
    "milestones"
})
public class Planning {
    @JsonProperty("rationale")
    @JsonPropertyDescription("The rationale for the procurement provided in free text. More detail can be provided in" +
        " an attached document.")
    @Pattern(regexp = "^(rationale_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})" +
        "(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String rationale;

    @JsonProperty("budget")
    @JsonPropertyDescription("This section contain information about the budget line, and associated projects, " +
        "through which this contracting process is funded. It draws upon data model of the [Fiscal Data Package]" +
        "(http://fiscal.dataprotocols.org/), and should be used to cross-reference to more detailed information held " +
        "using a Budget Data Package, or, where no linked Budget Data Package is available, to provide enough " +
        "information to allow a user to manually or automatically cross-reference with another published source of " +
        "budget and project information.")
    @Valid
    private final Budget budget;

    @JsonProperty("documents")
    @JsonPropertyDescription("A list of documents related to the planning process.")
    @Valid
    private final List<Document> documents;

    @JsonProperty("milestones")
    @JsonPropertyDescription("A list of milestones associated with the planning stage.")
    @Valid
    private final List<Milestone> milestones;

    @JsonCreator
    public Planning(@JsonProperty("budget") final Budget budget,
                    @JsonProperty("rationale") final String rationale,
                    @JsonProperty("documents") final List<Document> documents,
                    @JsonProperty("milestones") final List<Milestone> milestones) {
        this.budget = budget;
        this.rationale = rationale;
        this.documents = documents;
        this.milestones = milestones;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(budget)
                                    .append(rationale)
                                    .append(documents)
                                    .append(milestones)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Planning)) {
            return false;
        }
        final Planning rhs = (Planning) other;
        return new EqualsBuilder().append(rationale, rhs.rationale)
                                  .append(budget, rhs.budget)
                                  .append(documents, rhs.documents)
                                  .append(milestones, rhs.milestones)
                                  .isEquals();
    }
}
