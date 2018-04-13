package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum AwardCriteria {
    PRICE_ONLY("priceOnly"),
    COST_ONLY("costOnly"),
    QUALITY_ONLY("qualityOnly"),
    RATED_CRITERIA("ratedCriteria"),
    LOWEST_COST("lowestCost"),
    BEST_PROPOSAL("bestProposal"),
    BEST_VALUE_TO_GOVERNMENT("bestValueToGovernment"),
    SINGLE_BID_ONLY("singleBidOnly");

    private static final Map<String, AwardCriteria> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final AwardCriteria c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    AwardCriteria(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static AwardCriteria fromValue(final String value) {
        final AwardCriteria constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(AwardCriteria.class.getName(), value, Arrays.toString(values()));
        }
        return constant;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
