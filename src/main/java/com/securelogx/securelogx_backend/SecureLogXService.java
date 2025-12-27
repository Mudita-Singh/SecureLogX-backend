package com.securelogx.securelogx_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class SecureLogXService {

    private final Vault vault = new Vault();

    @Value("${securelogx.reports.dir}")
    private String reportsDir;

    // ================= ANALYZE + ENCRYPT =================
    public String analyzeAndEncrypt(String logPath, String password) throws Exception {

        LogReader reader = new LogReader();
        LogAnalyzer analyzer = new LogAnalyzer();
        ReportGenerator generator = new ReportGenerator(reportsDir);

        var logs = reader.readLogs(logPath);
        var incidents = analyzer.analyze(logs);

        String reportPath = generator.generate(incidents);
        return vault.encryptFile(reportPath, password);
    }

    // ================= DECRYPT =================
    public String decryptReport(String encryptedPath, String password) throws Exception {

        String projectRoot = System.getProperty("user.dir");
        String absolutePath = projectRoot + File.separator + encryptedPath;

        File file = new File(absolutePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("Encrypted file not found");
        }

        return vault.decryptFile(file.getAbsolutePath(), password);
    }
}
