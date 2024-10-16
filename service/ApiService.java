package com.example.api.service;

import com.example.api.util.ExcelToYamlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ApiService {

    @Autowired
    private ExcelToYamlConverter excelToYamlConverter;

    public String generateOasYaml(String filePath) throws IOException {
        // Call the method to convert Excel data to YAML using the template
        return excelToYamlConverter.convertToYaml(filePath);
    }
}
