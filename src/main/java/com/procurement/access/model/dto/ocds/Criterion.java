package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "source",
        "relatesTo",
        "relatedItem",
        "requirementGroups"
})
public class Criterion {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("source")
    private final Source source;

    @JsonProperty("relatesTo")
    private final RelatesTo relatesTo;

    @JsonProperty("relatedItem")
    private final String relatedItem;

    @JsonProperty("requirementGroups")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<RequirementGroup> requirementGroups;

    @JsonCreator
    public Criterion(@JsonProperty("id") final String id,
                     @JsonProperty("title") final String title,
                     @JsonProperty("description") final String description,
                     @JsonProperty("source") final Source source,
                     @JsonProperty("relatesTo") final RelatesTo relatesTo,
                     @JsonProperty("relatedItem") final String relatedItem,
                     @JsonProperty("requirementGroups") final LinkedHashSet<RequirementGroup> requirementGroups) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source = source;
        this.relatesTo = relatesTo;
        this.relatedItem = relatedItem;
        this.requirementGroups = requirementGroups;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(title)
                .append(description)
                .append(source)
                .append(relatesTo)
                .append(relatedItem)
                .append(requirementGroups)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Criterion)) {
            return false;
        }
        final Criterion rhs = (Criterion) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(source, rhs.source)
                .append(relatesTo, rhs.relatesTo)
                .append(relatedItem, rhs.relatedItem)
                .append(requirementGroups, rhs.requirementGroups)
                .isEquals();
    }

    public enum RelatesTo {
        ITEM("item"),
        TENDERER("tenderer");
        private final static Map<String, RelatesTo> CONSTANTS = new HashMap<>();

        static {
            for (final Criterion.RelatesTo c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        RelatesTo(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static RelatesTo fromValue(final String value) {
            final Criterion.RelatesTo constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
            }
            return constant;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }
    }

    public enum Source {
        TENDERER("tenderer"),
        BUYER("buyer"),
        PROCURING_ENTITY("procuringEntity");
        private final static Map<String, Source> CONSTANTS = new HashMap<>();

        static {
            for (final Criterion.Source c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        Source(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static Source fromValue(final String value) {
            final Source constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }
    }
}
