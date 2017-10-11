package com.ocds.tender.controller;

import com.ocds.tender.model.dto.DataDto;
import com.ocds.tender.service.MainServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/update")
public class MainController {

    private MainServiceImpl tenderService;

    public MainController(MainServiceImpl tenderService) {
        this.tenderService = tenderService;
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    public ResponseEntity<String> addTender(@RequestBody DataDto data) {
        tenderService.updateData(data);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
