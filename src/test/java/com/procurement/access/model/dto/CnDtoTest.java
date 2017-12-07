package com.procurement.access.model.dto;

import com.procurement.access.AbstractDomainObjectTest;
import com.procurement.access.model.dto.cn.CnDto;
import org.junit.jupiter.api.Test;

public class CnDtoTest extends AbstractDomainObjectTest {
    @Test
    public void testMappingToDocumentDto() {
        compare(CnDto.class, "json/cn.json");
    }

    @Test
    public void testMappingWithoutRequiredToDocumentDto() {
        compare(CnDto.class, "json/cn_without_required.json");
    }
}
