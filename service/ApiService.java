package com.example.api.service;

import com.example.api.util.ExcelToYamlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ApiService {

    @Autowired
    private ExcelToYamlConverter excelToYamlConverter;

    public String generateOasYaml(MultipartFile file) throws IOException {
        return excelToYamlConverter.convertToYaml(file);
    }
}
