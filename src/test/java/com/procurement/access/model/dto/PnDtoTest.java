package com.procurement.access.model.dto;

import com.procurement.access.AbstractDomainObjectTest;
import com.procurement.access.model.dto.pn.PnDto;
import org.junit.jupiter.api.Test;

public class PnDtoTest extends AbstractDomainObjectTest {
    @Test
    public void testMappingPnJsonToPnDto() {
        compare(PnDto.class, "json/pn.json");
    }

    @Test
    public void testMappingWithoutRequiredToDocumentDto() {
        compare(PnDto.class, "json/pn_without_required.json");
    }
}
