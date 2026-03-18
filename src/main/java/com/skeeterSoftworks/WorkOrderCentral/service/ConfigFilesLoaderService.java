package com.skeeterSoftworks.WorkOrderCentral.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ConfigFilesLoaderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String FI_PRECONDITIONS_FILE = "./config/FiPreconditions.json";

    public ArrayNode getWorkOrderPreconditions() throws IOException {


         File file = new File(FI_PRECONDITIONS_FILE);
         InputStream inputStream = new FileInputStream(file);

        JsonNode jsonNode = objectMapper.readTree(inputStream);

        if (jsonNode.isArray()) {
            return (ArrayNode) jsonNode;
        } else {
            throw new IllegalArgumentException("The provided JSON file does not contain a JSON array.");
        }
    }
}
