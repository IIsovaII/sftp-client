package org.example.services;

import org.example.Validator;
import org.example.dto.DomainIpRecord;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomainIpService {
    private List<DomainIpRecord> records;
    private static final Pattern DOMAIN_IP_OBJECT_PATTERN = Pattern.compile(
            "\\{\\s*\"domain\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"ip\"\\s*:\\s*\"([^\"]+)\"\\s*\\}"
    );

    public DomainIpService() {
        this.records = new ArrayList<>();
    }

    public void loadDataFromFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }

        this.records = new ArrayList<>();

        // Простой парсинг с помощью регулярных выражений
        String json = content.toString();
        Matcher matcher = DOMAIN_IP_OBJECT_PATTERN.matcher(json);

        while (matcher.find()) {
            DomainIpRecord record = new DomainIpRecord();
            record.setDomain(matcher.group(1));
            record.setIp(matcher.group(2));
            records.add(record);
        }
    }

    public String convertDataToJson() {
        if (records == null || records.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < records.size(); i++) {
            DomainIpRecord record = records.get(i);
            json.append("   {\n");
            json.append("       \"domain\": \"").append(escapeJson(record.getDomain())).append("\",\n");
            json.append("       \"ip\": \"").append(escapeJson(record.getIp())).append("\"\n");
            json.append("   }");

            if (i < records.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public List<DomainIpRecord> getAllRecords() {
        List<DomainIpRecord> sortedRecords = new ArrayList<>(records);
        sortedRecords.sort(Comparator.comparing(DomainIpRecord::getDomain));
        return sortedRecords;
    }

    public String findIpByDomain(String domain) {
        return records.stream()
                .filter(record -> record.getDomain().equalsIgnoreCase(domain))
                .findFirst()
                .map(DomainIpRecord::getIp)
                .orElse(null);
    }

    public String findDomainByIp(String ip) {
        if (Validator.isNotValidIPv4(ip)) {
            throw new IllegalArgumentException("Некорректный IPv4 адрес: " + ip);
        }

        return records.stream()
                .filter(record -> record.getIp().equals(ip))
                .findFirst()
                .map(DomainIpRecord::getDomain)
                .orElse(null);
    }

    public void addRecord(String domain, String ip) throws Exception {
        // Валидация IP
        if (Validator.isNotValidIPv4(ip)) {
            throw new IllegalArgumentException("Некорректный IPv4 адрес: " + ip);
        }

        // Проверка уникальности
        if (findIpByDomain(domain) != null) {
            throw new IllegalArgumentException("Домен '" + domain + "' уже существует");
        }
        if (findDomainByIp(ip) != null) {
            throw new IllegalArgumentException("IP-адрес '" + ip + "' уже существует");
        }

        records.add(new DomainIpRecord(domain, ip));
    }

    public void removeRecordByDomain(String domain) throws Exception {
        boolean removed = records.removeIf(record -> record.getDomain().equalsIgnoreCase(domain));
        if (!removed) {
            throw new IllegalArgumentException("Домен '" + domain + "' не найден");
        }
    }

    public void removeRecordByIp(String ip) throws Exception {
        if (Validator.isNotValidIPv4(ip)) {
            throw new IllegalArgumentException("Некорректный IPv4 адрес: " + ip);
        }
        boolean removed = records.removeIf(record -> record.getIp().equals(ip));
        if (!removed) {
            throw new IllegalArgumentException("IP-адрес '" + ip + "' не найден");
        }
    }

    public void saveDataToFile(String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            writer.write(convertDataToJson());
        }
    }
}
