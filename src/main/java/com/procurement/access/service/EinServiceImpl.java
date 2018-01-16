package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.ein.EinResponseDto;
import com.procurement.access.model.entity.EinEntity;
import com.procurement.access.repository.EinRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import org.springframework.stereotype.Service;

@Service
public class EinServiceImpl implements EinService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final EinRepository einRepository;

    public EinServiceImpl(final OCDSProperties ocdsProperties,
                          final JsonUtil jsonUtil,
                          final DateUtil dateUtil,
                          final EinRepository einRepository) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.einRepository = einRepository;
    }

    @Override
    public ResponseDto createEin(final String country,
                                 final String pmd,
                                 final String stage,
                                 final String owner,
                                 final EinDto ein) {

        final String cpId = ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC();
        ein.setOcId(cpId);
        setTenderId(ein, cpId);
        setBudgetId(ein);
        final EinEntity entity = einRepository.save(getEntity(ein, owner));
        return getResponseDto(cpId, entity.getToken(), ein);
    }

    @Override
    public ResponseDto updateEin(final EinDto ein) {
        return null;
    }

    private void setTenderId(final EinDto ein, final String cpId) {
        ein.getTender().setId(cpId);
    }

    private void setBudgetId(final EinDto ein) {
        ein.getPlanning().getBudget().setId(UUIDs.timeBased().toString());
    }

    private EinEntity getEntity(final EinDto ein, final String owner) {
        final EinEntity einEntity = new EinEntity();
        einEntity.setCpId(ein.getOcId());
        einEntity.setToken(UUIDs.timeBased().toString());
        einEntity.setOwner(owner);
        einEntity.setJsonData(jsonUtil.toJson(ein));
        return einEntity;
    }

    private ResponseDto getResponseDto(final String cpId, final String token, final EinDto ein) {
        final EinResponseDto responseDto = new EinResponseDto(
                token,
                cpId,
                ein.getId(),
                ein.getDate(),
                ein.getTag(),
                ein.getInitiationType(),
                ein.getLanguage(),
                ein.getPlanning(),
                ein.getTender(),
                ein.getParties(),
                ein.getBuyer(),
                ein.getRelatedProcesses()
        );
        return new ResponseDto(true, null, responseDto);
    }
}
