DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);  // Use block style for better readability
        options.setPrettyFlow(true);
        options.setIndent(4);  // Set indentation level
        options.setWidth(120);  // Set max line width for long lines

        // Handle multiline strings correctly
        Representer representer = new Representer();
        representer.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);

        // Initialize the YAML object with options
        Yaml yaml = new Yaml(representer, options);
        StringWriter writer = new StringWriter();

        // Convert the list of ApiDefinition to YAML and write to StringWriter
        yaml.dump(apiDefinitions, writer);

        // Return the formatted YAML string
        return writer.toString();
