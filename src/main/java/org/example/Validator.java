package org.example;

import org.example.dto.Arguments;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    public static Arguments validateAndSanitizeArguments(String[] args) {

        if (args.length != 4) {
            throw new IllegalArgumentException("Неверное количество аргументов");
        }

        // Стараемся избежать инъекций
        return new Arguments(
                validateHost(args[0]),
                validatePort(args[1]),
                validateUsername(args[2]),
                validatePassword(args[3])
        );
    }

    public static String validateHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Хост не может быть пустым");
        }
        host = host.trim();

        if (!host.matches("^[a-zA-Z0-9.-]+$")) {
            throw new IllegalArgumentException("Хост содержит запрещенные символы");
        }

        return host;
    }

    public static int validatePort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Порт должен быть в диапазоне 1-65535: " + port);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Порт должен быть числом: " + portStr);
        }
    }

    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        String trimmed = username.trim();

        if (!trimmed.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("Имя пользователя содержит запрещенные символы.");
        }

        if (trimmed.matches("^[-].*")) {
            throw new IllegalArgumentException("Имя пользователя не может начинаться с дефиса");
        }

        if (trimmed.endsWith(".") || trimmed.endsWith("-")) {
            throw new IllegalArgumentException("Имя пользователя не может заканчиваться на точку или дефис");
        }

        return trimmed;
    }

    public static String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        password = password.trim();

        if (password.matches(".*[;|&$><].*")) {
            throw new IllegalArgumentException("Пароль содержит запрещенные символы");
        }

        if (password.length() > 100) {
            throw new IllegalArgumentException("Пароль слишком длинный (макс. 100 символов)");
        }

        return password;
    }

    public static boolean isNotValidIPv4(String ip) {
        return !IPV4_PATTERN.matcher(ip).matches();
    }
}
