package com.ocds.etender.controller;

import com.ocds.etender.model.dto.DataDto;
import com.ocds.etender.service.MainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/update")
public class MainController {

    private MainService tenderService;

    public MainController(MainService tenderService) {
        this.tenderService = tenderService;
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    public ResponseEntity<String> addTender(@RequestBody DataDto data) {
        tenderService.updateData(data);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
