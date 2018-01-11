package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import org.springframework.stereotype.Service;

@Service
public interface FsService {

    ResponseDto createFs(String country,
                         String pmd,
                         String stage,
                         String owner,
                         FsDto fsDto);

    ResponseDto updateFs(FsDto fsDto);
}
