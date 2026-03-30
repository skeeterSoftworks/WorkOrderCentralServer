package com.skeeterSoftworks.WorkOrderCentral.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SelectOptionsTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@Component
public class ConfigFilesLoaderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String WORK_STATION_PRECONDITIONS_FILE = "./config/WorkStationPreconditions.json";
    private final String SELECT_OPTIONS_FILE = "./config/SelectOptions.json";

    public ArrayNode getWorkOrderPreconditions() throws IOException {

         File file = new File(WORK_STATION_PRECONDITIONS_FILE);
         InputStream inputStream = new FileInputStream(file);

        JsonNode jsonNode = objectMapper.readTree(inputStream);

        if (jsonNode.isArray()) {
            return (ArrayNode) jsonNode;
        } else {
            throw new IllegalArgumentException("The provided JSON file does not contain a JSON array.");
        }
    }

    public SelectOptionsTO readSelectOptions() throws IOException {
        File file = new File(SELECT_OPTIONS_FILE);
        if (!file.isFile()) {
            return defaultSelectOptions();
        }
        try (InputStream in = new FileInputStream(file)) {
            SelectOptionsTO parsed = objectMapper.readValue(in, SelectOptionsTO.class);
            if (parsed.getMeasuringTools() == null) {
                parsed.setMeasuringTools(new ArrayList<>());
            }
            if (parsed.getDeliveryTerms() == null) {
                parsed.setDeliveryTerms(new ArrayList<>());
            }
            if (parsed.getRejectCauses() == null) {
                parsed.setRejectCauses(new ArrayList<>());
            }
            return parsed;
        }
    }

    /**
     * Merges non-null list fields from {@code incoming} into the current file (or defaults),
     * so partial updates (e.g. from older clients) do not wipe other option lists.
     */
    public void writeSelectOptions(SelectOptionsTO incoming) throws IOException {
        SelectOptionsTO existing = readSelectOptions();
        if (incoming.getMeasuringTools() != null) {
            existing.setMeasuringTools(new ArrayList<>(incoming.getMeasuringTools()));
        }
        if (incoming.getDeliveryTerms() != null) {
            existing.setDeliveryTerms(new ArrayList<>(incoming.getDeliveryTerms()));
        }
        if (incoming.getRejectCauses() != null) {
            existing.setRejectCauses(new ArrayList<>(incoming.getRejectCauses()));
        }
        File file = new File(SELECT_OPTIONS_FILE);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create directory: " + parent);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, existing);
    }

    private static SelectOptionsTO defaultSelectOptions() {
        SelectOptionsTO d = new SelectOptionsTO();
        d.setMeasuringTools(new ArrayList<>(java.util.List.of(
                "Pomično merilo",
                "Specijalni uređaj",
                "CMM",
                "Rugozimetar",
                "Dubinomer",
                "Profiltester",
                "Visinomer",
                "Kontrolni čep",
                "Mikroskop"
        )));
        d.setDeliveryTerms(new ArrayList<>(java.util.List.of("EXW", "FOB", "CIF", "DDP")));
        return d;
    }
}
