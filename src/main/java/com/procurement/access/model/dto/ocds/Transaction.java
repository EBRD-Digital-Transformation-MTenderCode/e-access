package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.net.URI;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "source",
    "date",
    "value",
    "payer",
    "payee",
    "uri",
    "amount",
    "providerOrganization",
    "receiverOrganization"
})
public class Transaction {
    @JsonProperty("id")
    @JsonPropertyDescription("A unique identifier for this transaction. This identifier should be possible to " +
        "cross-reference against the provided data source. For IATI this is the transaction reference.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("source")
    @JsonPropertyDescription("Used to point either to a corresponding Fiscal Data Package, IATI file, or machine or " +
        "human-readable source where users can find further information on the budget line item identifiers, or " +
        "project identifiers, provided here.")
    private final URI source;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date of the transaction")
    private final LocalDateTime date;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("payer")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
        "section")
    @Valid
    private final OrganizationReference payer;

    @JsonProperty("payee")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
        "section")
    @Valid
    private final OrganizationReference payee;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI pointing directly to a machine-readable record about this spending transaction.")
    private final URI uri;

    @JsonProperty("amount")
    @Valid
    private final Value amount;

    @JsonProperty("providerOrganization")
    @Valid
    private final Identifier providerOrganization;

    @JsonProperty("receiverOrganization")
    @Valid
    private final Identifier receiverOrganization;

    @JsonCreator
    public Transaction(@JsonProperty("id") final String id,
                       @JsonProperty("source") final URI source,
                       @JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
                       @JsonProperty("value") final Value value,
                       @JsonProperty("payer") final OrganizationReference payer,
                       @JsonProperty("payee") final OrganizationReference payee,
                       @JsonProperty("uri") final URI uri,
                       @JsonProperty("amount") final Value amount,
                       @JsonProperty("providerOrganization") final Identifier providerOrganization,
                       @JsonProperty("receiverOrganization") final Identifier receiverOrganization) {
        this.id = id;
        this.source = source;
        this.date = date;
        this.value = value;
        this.payer = payer;
        this.payee = payee;
        this.uri = uri;
        this.amount = amount;
        this.providerOrganization = providerOrganization;
        this.receiverOrganization = receiverOrganization;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(source)
                                    .append(date)
                                    .append(value)
                                    .append(payer)
                                    .append(payee)
                                    .append(uri)
                                    .append(amount)
                                    .append(providerOrganization)
                                    .append(receiverOrganization)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Transaction)) {
            return false;
        }
        final Transaction rhs = (Transaction) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(source, rhs.source)
                                  .append(date, rhs.date)
                                  .append(value, rhs.value)
                                  .append(payer, rhs.payer)
                                  .append(payee, rhs.payee)
                                  .append(uri, rhs.uri)
                                  .append(amount, rhs.amount)
                                  .append(providerOrganization, rhs.providerOrganization)
                                  .append(receiverOrganization, rhs.receiverOrganization)
                                  .isEquals();
    }
}
