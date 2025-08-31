package org.example;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.example.dto.Arguments;
import org.example.services.DomainIpService;
import org.example.services.SftpService;

import java.io.IOException;

import static org.example.Validator.validateAndSanitizeArguments;

public class Main {
    private static final String PATH_TO_DOMAINS = "/upload/domains.json";

    public static void main(String[] args) {
        try {
            Arguments validatedArgs = validateAndSanitizeArguments(args);

            SftpService sftpService = null;
            DomainIpService domainIpService = new DomainIpService();

            try {
                sftpService = createSftpService(validatedArgs);
                connectAndProcessSftp(validatedArgs, sftpService, domainIpService);
                runConsoleMenu(sftpService, domainIpService);
            } catch (JSchException | SftpException e) {
                handleSftpError(e, validatedArgs);
            } catch (IOException e) {
                handleIoError(e);
            } catch (Exception e) {
                handleUnexpectedError(e);
            } finally {
                safeDisconnect(sftpService);
            }

        } catch (IllegalArgumentException e) {
            handleValidationError(e);
        }
    }

    // Вспомогательные функции
    private static SftpService createSftpService(Arguments args) {
        return new SftpService(args.getHost(), args.getPort(),
                args.getUsername(), args.getPassword());
    }

    private static void connectAndProcessSftp(Arguments validatedArgs,
                                              SftpService sftpService,
                                              DomainIpService domainIpService)
            throws JSchException, SftpException, IOException {

        System.out.println("Подключение к SFTP-серверу " +
                validatedArgs.getHost() + ":" + validatedArgs.getPort() + "...");
        sftpService.connect();
        System.out.println("Подключение установлено успешно!");
        String localFilePath = sftpService.downloadFile(PATH_TO_DOMAINS);
        domainIpService.loadDataFromFile(localFilePath);
        System.out.println("Данные успешно загружены из файла.");
    }

    private static void runConsoleMenu(SftpService sftpService,
                                       DomainIpService domainIpService) {
        ConsoleMenu menu = new ConsoleMenu(sftpService, domainIpService, PATH_TO_DOMAINS);
        menu.start();
    }

    private static void safeDisconnect(SftpService sftpService) {
        if (sftpService != null) {
            try {
                sftpService.disconnect();
                System.out.println("Соединение закрыто.");
            } catch (Exception e) {
                System.err.println("Ошибка при закрытии соединения: " +
                        e.getMessage());
            }
        }
    }

    // Обработка ошибок

    private static void handleSftpError(Exception e, Arguments args) {
        System.err.println("Ошибка SFTP: " + e.getMessage());
        System.err.println("Проверьте параметры подключения:");
        System.err.println("  Хост: " + args.getHost());
        System.err.println("  Порт: " + args.getPort());
        System.err.println("  Пользователь: " + args.getUsername());
        System.err.println("Убедитесь, что сервер доступен и credentials корректны.");
    }

    private static void handleIoError(IOException e) {
        System.err.println("Ошибка работы с файлом: " + e.getMessage());
        System.err.println("Проверьте права доступа и наличие файла на сервере.");
    }

    private static void handleUnexpectedError(Exception e) {
        System.err.println("Неожиданная ошибка: " + e.getMessage());
        e.printStackTrace();
    }

    private static void handleValidationError(IllegalArgumentException e) {
        System.err.println("Ошибка валидации: " + e.getMessage());
        printUsage();
        System.exit(1);
    }

    private static void printUsage() {
        System.err.println("\nИспользование: java -jar sftp-client.jar <host> <port> <username> <password>");
        System.err.println("Пример: java -jar sftp-client.jar localhost 2222 user password");
        System.err.println("\nПравила валидации:");
        System.err.println("    - host: только буквы, цифры, точки, дефисы");
        System.err.println("    - port: число 1-65535");
        System.err.println("    - username: только буквы, цифры, подчеркивания, дефисы");
        System.err.println("    - password: не должен содержать специальные символы (;|&$><)");
    }
}