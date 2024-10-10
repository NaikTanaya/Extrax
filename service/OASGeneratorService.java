package com.example.api.service;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Service
public class OASGeneratorService {

    public String generateOAS(Map<String, Object> apiInfo) {
        // Configure SnakeYAML options
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        // Convert Java object (Map) to YAML using SnakeYAML
        Yaml yaml = new Yaml(options);
        return yaml.dump(apiInfo);
    }
}
