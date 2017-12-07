package com.procurement.access.model.dto;

import com.procurement.access.AbstractDomainObjectTest;
import com.procurement.access.model.dto.pin.PinDto;
import org.junit.jupiter.api.Test;

public class PinDtoTest extends AbstractDomainObjectTest {
    @Test
    public void testMappingToDocumentDto() {
        compare(PinDto.class, "json/pin.json");
    }

    @Test
    public void testMappingWithoutRequiredToDocumentDto() {
        compare(PinDto.class, "json/pin_without_required.json");
    }
}
