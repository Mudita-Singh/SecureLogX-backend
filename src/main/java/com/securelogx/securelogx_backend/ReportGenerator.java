package com.securelogx.securelogx_backend;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    private final String reportsDir;

    public ReportGenerator(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    public String generate(List<Incident> incidents) throws Exception {

        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        String fileName = "incident_report_" + timestamp + ".json";
        File reportFile = new File(dir, fileName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(reportFile, incidents);

        return reportFile.getPath();
    }
}
