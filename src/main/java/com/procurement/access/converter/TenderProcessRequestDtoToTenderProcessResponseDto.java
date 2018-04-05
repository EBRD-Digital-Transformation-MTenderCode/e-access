package com.procurement.access.converter;

import com.procurement.access.model.dto.ocds.Tender;
import com.procurement.access.model.dto.tender.TenderProcessRequestDto;
import com.procurement.access.model.dto.tender.TenderProcessResponseDto;
import com.procurement.submission.model.dto.request.PeriodDataDto;
import com.procurement.submission.model.dto.request.TenderPeriodDto;
import com.procurement.submission.model.entity.SubmissionPeriodEntity;
import org.springframework.core.convert.converter.Converter;

public class TenderProcessRequestDtoToTenderProcessResponseDto implements Converter<TenderProcessRequestDto, TenderProcessResponseDto> {

    @Override
    public TenderProcessResponseDto convert(final TenderProcessRequestDto requestDto) {
        final TenderProcessResponseDto responseDto =
                new TenderProcessResponseDto(
                        null,
                        null,
                        requestDto.getPlanning(),
                        new Tender(
                                null,
                                requestDto.getTender().getTitle(),
                                requestDto.getTender().getDescription(),
                                requestDto.getTender().getStatus(),
                                requestDto.getTender().getStatusDetails(),
                                requestDto.getTender().getClassification(),
                                requestDto.getTender().getAcceleratedProcedure(),
                                requestDto.getTender().getDesignContest(),
                                requestDto.getTender().getElectronicWorkflows(),
                                requestDto.getTender().getJointProcurement(),
                                requestDto.getTender().getProcedureOutsourcing(),
                                requestDto.getTender().getFramework(),
                                requestDto.getTender().getDynamicPurchasingSystem(),
                                requestDto.getTender().getLegalBasis(),
                                requestDto.getTender().getProcurementMethod(),
                                requestDto.getTender().getProcurementMethodDetails(),
                                requestDto.getTender().getProcurementMethodRationale(),
                                mainProcurementCategory
                                additionalProcurementCategories
                                awardCriteria
                                submissionMethod
                                submissionMethodDetails
                                tenderPeriod
                                eligibilityCriteria
                                contractPeriod
                                procuringEntity
                                documents
                                lots
                                lotGroups
                                acceleratedProcedure
                                designContest
                                electronicWorkflows
                                jointProcurement
                                legalBasis
                                procedureOutsourcing
                        )

                );

        return responseDto;
    }
}
