package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import org.springframework.stereotype.Service;

@Service
public interface FsService {

    ResponseDto createFs(String cpId,
                         String owner,
                         FsDto fsDto);

    ResponseDto updateFs(String cpId,
                         String token,
                         String owner,
                         FsDto fsDto);
}
