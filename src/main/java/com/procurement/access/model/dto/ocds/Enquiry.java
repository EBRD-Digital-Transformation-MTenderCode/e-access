package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "date",
        "author",
        "title",
        "description",
        "answer",
        "dateAnswered",
        "relatedItem",
        "relatedLot",
        "threadID"
})
public class Enquiry {
    @JsonProperty("id")
    private final String id;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime date;

    @JsonProperty("author")
    @Valid
    private final Author author;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("answer")
    private final String answer;

    @JsonProperty("dateAnswered")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime dateAnswered;

    @JsonProperty("relatedItem")
    private final String relatedItem;

    @JsonProperty("relatedLot")
    private final String relatedLot;

    @JsonProperty("threadID")
    private final String threadID;

    @JsonCreator
    public Enquiry(@JsonProperty("id") final String id,
                   @JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
                   @JsonProperty("author") final Author author,
                   @JsonProperty("title") final String title,
                   @JsonProperty("description") final String description,
                   @JsonProperty("answer") final String answer,
                   @JsonProperty("dateAnswered") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime dateAnswered,
                   @JsonProperty("relatedItem") final String relatedItem,
                   @JsonProperty("relatedLot") final String relatedLot,
                   @JsonProperty("threadID") final String threadID) {
        this.id = id;
        this.date = date;
        this.author = author;
        this.title = title;
        this.description = description;
        this.answer = answer;
        this.dateAnswered = dateAnswered;
        this.relatedItem = relatedItem;
        this.relatedLot = relatedLot;
        this.threadID = threadID;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(date)
                .append(author)
                .append(title)
                .append(description)
                .append(answer)
                .append(dateAnswered)
                .append(relatedItem)
                .append(relatedLot)
                .append(threadID)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Enquiry)) {
            return false;
        }
        final Enquiry rhs = (Enquiry) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(date, rhs.date)
                .append(author, rhs.author)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(answer, rhs.answer)
                .append(dateAnswered, rhs.dateAnswered)
                .append(relatedItem, rhs.relatedItem)
                .append(relatedLot, rhs.relatedLot)
                .append(threadID, rhs.threadID)
                .isEquals();
    }
}
