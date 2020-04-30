package com.bywim.visuallog.controller;

import com.bywim.visuallog.service.JSchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("jsch")
public class JSchController {

    @Autowired
    private JSchService logService;

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "hello";
    }

    @RequestMapping("/read-log")
    @ResponseBody
    public String readLog(String logFile, @RequestParam(value = "lineNum", required = false) Integer lineNum){

        String result = logService.readLog(logFile, lineNum);
        return result;
    }

    @RequestMapping("/upload-file")
    @ResponseBody
    public boolean uploadFile(String logFile, @RequestParam(value = "lineNum", required = false) Integer lineNum){

        boolean result = logService.uploadFile("","");
        return result;
    }
}
