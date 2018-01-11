package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.ein.EinResponseDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import com.procurement.access.model.entity.EinEntity;
import com.procurement.access.repository.EinRepository;
import com.procurement.access.repository.FsRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import org.springframework.stereotype.Service;

@Service
public class EinServiceImpl implements EinService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final EinRepository einRepository;
    private final FsRepository fsRepository;

    public EinServiceImpl(final OCDSProperties ocdsProperties,
                          final JsonUtil jsonUtil,
                          final DateUtil dateUtil,
                          final EinRepository einRepository,
                          final FsRepository fsRepository) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.einRepository = einRepository;
        this.fsRepository = fsRepository;
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
        final EinEntity entity = einRepository.save(getEntity(ein, owner, UUIDs.timeBased().toString()));
        return getResponseDto(ein, entity);
    }

    @Override
    public ResponseDto updateEin(final EinDto ein) {
        return null;
    }

    @Override
    public ResponseDto updateAmountByFs(final UpdateFsDto updateFs) {
        final EinEntity entity = einRepository.getLastByOcId(updateFs.getCpId());
        final EinDto ein = jsonUtil.toObject(EinDto.class, entity.getJsonData());
        final Double totalAmount = fsRepository.getTotalAmountByCpId(updateFs.getCpId());
        ein.getPlanning().getBudget().getAmount().setAmount(totalAmount);
        final EinEntity newEntity = einRepository.save(getEntity(ein, entity.getOwner(), entity.getToken()));
        return getResponseDto(ein, newEntity);
    }

    private void setTenderId(final EinDto ein, final String cpId) {
        ein.getTender().setId(cpId);
    }

    private void setBudgetId(final EinDto ein) {
        ein.getPlanning().getBudget().setId(UUIDs.timeBased().toString());
    }

    private EinEntity getEntity(final EinDto ein, final String owner, final String token) {
        final EinEntity einEntity = new EinEntity();
        einEntity.setCpId(ein.getOcId());
        einEntity.setToken(token);
        einEntity.setOwner(owner);
        einEntity.setJsonData(jsonUtil.toJson(ein));
        return einEntity;
    }

    private ResponseDto getResponseDto(final EinDto ein, final EinEntity entity) {
        final EinResponseDto responseDto = new EinResponseDto(
                entity.getToken(),
                entity.getCpId(),
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


//    @Override
//    public void addRelatedProcess(final UpdateFsDto updateFsDto) {
//        final EinEntity einEntity = einRepository.getLastByOcId(updateFsDto.getCpId());
//        final EinDto einDto = jsonUtil.toObject(EinDto.class, einEntity.getJsonData());
//        addFsRelatedProcessToEin(einDto, updateFsDto.getOcId());
////        final Double totalAmount = fsService.getTotalAmountFs(updateFsDto.getCpId());
////        einDto.getPlanning().getBudget().getAmount().setAmount(totalAmount);
//        einRepository.save(getEntity(einDto));
////        return getResponseDto(einDto);
//    }

//    private void addFsRelatedProcessToEin(final EinDto ein, final String ocId) {
//        final EinRelatedProcessDto relatedProcess = new EinRelatedProcessDto();
//        relatedProcess.setId(UUIDs.timeBased()
//                .toString());
//        relatedProcess.setRelationship(Arrays.asList(EinRelatedProcessDto.RelatedProcessType.FRAMEWORK));
//        relatedProcess.setScheme(EinRelatedProcessDto.RelatedProcessScheme.OCID);
//        relatedProcess.setIdentifier(ocId);
//        final List<EinRelatedProcessDto> relatedProcesses = ein.getRelatedProcesses();
//        relatedProcesses.add(relatedProcess);
//        ein.setRelatedProcesses(relatedProcesses);
//    }
}
