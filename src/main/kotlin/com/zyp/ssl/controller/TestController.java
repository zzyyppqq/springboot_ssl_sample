package com.zyp.ssl.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/secure-data")
    public String getSecureData() {
        return "This is secure data from server with SSL Pinning and Mutual Authentication!";
    }
}