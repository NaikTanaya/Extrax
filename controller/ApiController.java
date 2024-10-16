package com.example.apidetails.controller;

import com.example.apidetails.service.ApiDetailsExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ApiDetailsController {

    @Autowired
    private ApiDetailsExtractorService apiDetailsExtractorService;

    @GetMapping("/")
    public String showForm() {
        return "form";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String result = apiDetailsExtractorService.extractApiDetails(file);
            model.addAttribute("result", result);
            model.addAttribute("message", "File processed successfully!");
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred while processing the file.");
            e.printStackTrace();
        }
        return "result";
    }
}





<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OAS YAML Result</title>
</head>
<body>
    <h2>Generated OpenAPI Specification</h2>
    <pre>{{result}}</pre>
    <p>{{message}}</p>
</body>
</html>

    
