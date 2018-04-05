package com.procurement.access.converter;

import com.procurement.submission.model.dto.request.PeriodDataDto;
import com.procurement.submission.model.dto.request.TenderPeriodDto;
import com.procurement.submission.model.entity.SubmissionPeriodEntity;
import org.springframework.core.convert.converter.Converter;

public class PeriodDataDtoToPeriodEntity implements Converter<PeriodDataDto, SubmissionPeriodEntity> {

    @Override
    public SubmissionPeriodEntity convert(final PeriodDataDto dataDto) {
        final SubmissionPeriodEntity submissionPeriodEntity = new SubmissionPeriodEntity();
        submissionPeriodEntity.setOcId(dataDto.getOcId());
        final TenderPeriodDto tenderPeriod = dataDto.getTenderPeriod();
        submissionPeriodEntity.setStartDate(tenderPeriod.getStartDate());
        submissionPeriodEntity.setEndDate(tenderPeriod.getEndDate());
        return submissionPeriodEntity;
    }
}
