package com.ocds.access.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/create")
public class CreateController {

    @RequestMapping(value = "/cin", method = RequestMethod.GET)
    public ResponseEntity<String> insertCin() {
        return new ResponseEntity<>("ok111", HttpStatus.OK);
    }
}
