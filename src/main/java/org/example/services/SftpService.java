package org.example.services;

import com.jcraft.jsch.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SftpService {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private Session session;
    private ChannelSftp channel;

    public SftpService(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() throws JSchException, SftpException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        // Отключаем проверку хоста (для тестовых сред)
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();
        Channel sessionChannel = session.openChannel("sftp");
        sessionChannel.connect();
        this.channel = (ChannelSftp) sessionChannel;
    }

    public String downloadFile(String remoteFilePath) throws SftpException, IOException {
        File tempFile = File.createTempFile("domains", ".json");
        tempFile.deleteOnExit();

        OutputStream outputStream = Files.newOutputStream(tempFile.toPath());
        channel.get(remoteFilePath, outputStream);
        outputStream.close();

        return tempFile.getAbsolutePath();
    }

    public void uploadFile(String localFilePath, String remoteFilePath) throws SftpException, IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(localFilePath))) {
            channel.put(inputStream, remoteFilePath);
        }
    }

    public void disconnect() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}