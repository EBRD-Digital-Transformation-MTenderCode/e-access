
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title",
    "type",
    "description",
    "code",
    "dueDate",
    "dateMet",
    "dateModified",
    "status",
    "documents",
    "relatedLots",
    "relatedParties",
    "additionalInformation"
})
public class Milestone {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this milestone, unique within this block. This field is used to " +
        "keep track of multiple revisions of a milestone through the compilation from release to record mechanism.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("Milestone title")
    @Pattern(regexp = "^(title_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-" +
        "([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String title;

    @JsonProperty("type")
    @JsonPropertyDescription("The type of milestone, drawn from an extended [milestoneType codelist](http://standard" +
        ".open-contracting.org/latest/en/schema/codelists/#milestone-type).")
    private final MilestoneType type;

    @JsonProperty("description")
    @JsonPropertyDescription("A description of the milestone.")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String description;

    @JsonProperty("code")
    @JsonPropertyDescription("Milestone codes can be used to track specific events that take place for a particular " +
        "kind of contracting process. For example, a code of 'approvalLetter' could be used to allow applications to " +
        "understand this milestone represents the date an approvalLetter is due or signed. Milestone codes is an open" +
        " codelist, and codes should be agreed among data producers and the applications using that data.")
    private final String code;

    @JsonProperty("dueDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date the milestone is due.")
    private final LocalDateTime dueDate;

    @JsonProperty("dateMet")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date on which the milestone was met.")
    private final LocalDateTime dateMet;

    @JsonProperty("dateModified")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date the milestone was last reviewed or modified and the status was altered or " +
        "confirmed to still be correct.")
    private final LocalDateTime dateModified;

    @JsonProperty("status")
    @JsonPropertyDescription("The status that was realized on the date provided in dateModified, drawn from the " +
        "[milestoneStatus codelist](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#milestone-status).")
    private final Status status;

    @JsonProperty("documents")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("List of documents associated with this milestone (Deprecated in 1.1).")
    @Valid
    private final Set<Document> documents;

    @JsonProperty("relatedLots")
    @JsonPropertyDescription("If this milestone relates to a particular lot, provide the identifier(s) of the related" +
        " lot(s) here.")
    private final List<String> relatedLots;

    @JsonProperty("relatedParties")
    @JsonPropertyDescription("Parties that have a relationship with the milestone.")
    @Valid
    private final List<OrganizationReference> relatedParties;

    @JsonProperty("additionalInformation")
    @JsonPropertyDescription("Additional information about the milestone")
    private final String additionalInformation;

    public Milestone(@JsonProperty("id") final String id,
                     @JsonProperty("title") final String title,
                     @JsonProperty("type") final MilestoneType type,
                     @JsonProperty("description") final String description,
                     @JsonProperty("code") final String code,
                     @JsonProperty("dueDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime dueDate,
                     @JsonProperty("dateMet") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime dateMet,
                     @JsonProperty("dateModified") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime dateModified,
                     @JsonProperty("status") final Status status,
                     @JsonProperty("documents") final LinkedHashSet<Document> documents,
                     @JsonProperty("relatedLots") final List<String> relatedLots,
                     @JsonProperty("relatedParties") final List<OrganizationReference> relatedParties,
                     @JsonProperty("additionalInformation") final String additionalInformation) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.code = code;
        this.dueDate = dueDate;
        this.dateMet = dateMet;
        this.dateModified = dateModified;
        this.status = status;
        this.documents = documents;
        this.relatedLots = relatedLots;
        this.relatedParties = relatedParties;
        this.additionalInformation = additionalInformation;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(type)
                                    .append(description)
                                    .append(code)
                                    .append(dueDate)
                                    .append(dateMet)
                                    .append(dateModified)
                                    .append(status)
                                    .append(documents)
                                    .append(relatedLots)
                                    .append(relatedParties)
                                    .append(additionalInformation)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Milestone)) {
            return false;
        }
        final Milestone rhs = (Milestone) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(type, rhs.type)
                                  .append(description, rhs.description)
                                  .append(code, rhs.code)
                                  .append(dueDate, rhs.dueDate)
                                  .append(dateMet, rhs.dateMet)
                                  .append(dateModified, rhs.dateModified)
                                  .append(status, rhs.status)
                                  .append(documents, rhs.documents)
                                  .append(relatedLots, rhs.relatedLots)
                                  .append(relatedParties, rhs.relatedParties)
                                  .append(additionalInformation, rhs.additionalInformation)
                                  .isEquals();
    }

    public enum Status {
        SCHEDULED("scheduled"),
        MET("met"),
        NOT_MET("notMet"),
        PARTIALLY_MET("partiallyMet");

        private final String value;
        private final static Map<String, Status> CONSTANTS = new HashMap<>();

        static {
            for (final Status c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Status(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Status fromValue(final String value) {
            final Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

    }

    public enum MilestoneType {
        PRE_PROCUREMENT("preProcurement"),
        APPROVAL("approval"),
        ENGAGEMENT("engagement"),
        ASSESSMENT("assessment"),
        DELIVERY("delivery"),
        REPORTING("reporting"),
        FINANCING("financing");

        private final String value;
        private final static Map<String, MilestoneType> CONSTANTS = new HashMap<>();

        static {
            for (final MilestoneType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private MilestoneType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static MilestoneType fromValue(final String value) {
            final MilestoneType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

    }
}
