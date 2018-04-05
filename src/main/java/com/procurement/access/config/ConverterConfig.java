package com.procurement.access.config;

import com.procurement.submission.converter.BidQualificationDtoToBidResponse;
import com.procurement.submission.converter.PeriodDataDtoToPeriodEntity;
import com.procurement.submission.converter.QualificationOfferDtoToBidEntity;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class ConverterConfig {
    @Bean
    public ConversionServiceFactoryBean conversionService() {
        final Set<Converter> converters = new HashSet<>();
        converters.add(new PeriodDataDtoToPeriodEntity());
        converters.add(new QualificationOfferDtoToBidEntity());
        converters.add(new BidQualificationDtoToBidResponse());
        final ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.setConverters(converters);
        return bean;
    }
}
