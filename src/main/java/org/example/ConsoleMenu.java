package org.example;

import org.example.dto.DomainIpRecord;
import org.example.services.DomainIpService;
import org.example.services.SftpService;

import java.io.*;
import java.util.*;

public class ConsoleMenu {
    private final Scanner scanner;
    private final SftpService sftpService;
    private final DomainIpService domainIpService;
    private final String remoteFilePath;
    private final PrintStream output;

    public ConsoleMenu(SftpService sftpService, DomainIpService domainIpService, String remoteFilePath) {
        this.scanner = new Scanner(System.in);
        this.sftpService = sftpService;
        this.output = System.out;
        this.domainIpService = domainIpService;
        this.remoteFilePath = remoteFilePath;
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = getIntInput("Выберите действие: ");

            try {
                switch (choice) {
                    case 1:
                        showAllRecords();
                        break;
                    case 2:
                        findIpByDomain();
                        break;
                    case 3:
                        findDomainByIp();
                        break;
                    case 4:
                        addNewRecord();
                        break;
                    case 5:
                        removeRecord();
                        break;
                    case 6:
                        running = false;
                        break;
                    default:
                        output.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                output.println("Ошибка: " + e.getMessage());
            }

            if (running) {
                output.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }

        output.println("Завершение работы.");
        scanner.close();
    }

    private void printMenu() {
        output.println("\n=== SFTP Client Menu ===");
        output.println("1. Показать все записи");
        output.println("2. Найти IP по домену");
        output.println("3. Найти домен по IP");
        output.println("4. Добавить новую запись");
        output.println("5. Удалить запись");
        output.println("6. Выход");
        output.println("========================");
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                output.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                output.println("Пожалуйста, введите число.");
            }
        }
    }

    private void showAllRecords() {
        List<DomainIpRecord> records = domainIpService.getAllRecords();
        if (records.isEmpty()) {
            output.println("Список записей пуст.");
            return;
        }

        output.println("\nСписок доменов и IP-адресов:");
        for (int i = 0; i < records.size(); i++) {
            output.println((i + 1) + ". " + records.get(i));
        }
    }

    private void findIpByDomain() {
        output.print("Введите доменное имя: ");
        String domain = scanner.nextLine().trim();

        String ip = domainIpService.findIpByDomain(domain);
        if (ip != null) {
            output.println("IP-адрес для '" + domain + "': " + ip);
        } else {
            output.println("Домен '" + domain + "' не найден.");
        }
    }

    private void findDomainByIp() {
        output.print("Введите IP-адрес: ");
        String ip = scanner.nextLine().trim();

        String domain = domainIpService.findDomainByIp(ip);
        if (domain != null) {
            output.println("Домен для IP '" + ip + "': " + domain);
        } else {
            output.println("IP-адрес '" + ip + "' не найден.");
        }
    }

    private void addNewRecord() {
        try {
            output.print("Введите доменное имя: ");
            String domain = scanner.nextLine().trim();

            output.print("Введите IP-адрес: ");
            String ip = scanner.nextLine().trim();

            domainIpService.addRecord(domain, ip);
            saveChangesToServer();
            output.println("Запись успешно добавлена.");

        } catch (Exception e) {
            output.println("Ошибка при добавлении: " + e.getMessage());
        }
    }

    private void removeRecord() {
        output.println("Удалить по:");
        output.println("1. Доменному имени");
        output.println("2. IP-адресу");

        int choice = getIntInput("Выберите вариант: ");

        try {
            if (choice == 1) {
                output.print("Введите доменное имя для удаления: ");
                String domain = scanner.nextLine().trim();
                domainIpService.removeRecordByDomain(domain);
                saveChangesToServer();
                output.println("Запись успешно удалена.");

            } else if (choice == 2) {
                output.print("Введите IP-адрес для удаления: ");
                String ip = scanner.nextLine().trim();
                domainIpService.removeRecordByIp(ip);
                saveChangesToServer();
                output.println("Запись успешно удалена.");

            } else {
                output.println("Неверный выбор.");
            }
        } catch (Exception e) {
            output.println("Ошибка при удалении: " + e.getMessage());
        }
    }

    private void saveChangesToServer() {
        try {
            File tempFile = File.createTempFile("domains_update", ".json");
            domainIpService.saveDataToFile(tempFile.getAbsolutePath());
            sftpService.uploadFile(tempFile.getAbsolutePath(), remoteFilePath);
            tempFile.delete();

        } catch (Exception e) {
            output.println("Ошибка при сохранении на сервер: " + e.getMessage());
        }
    }
}
