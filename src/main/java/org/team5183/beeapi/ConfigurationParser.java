package org.team5183.beeapi;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ConfigurationParser {
    private static Configuration config;
    public static Configuration getConfiguration() {
        return config;
    }

    public static @Nullable Configuration parseConfiguration(String path) throws IOException, ConfigurationParseError {
        File configFile = new File(path);
        if (!configFile.exists()) {
            InputStream defaultConfig = ConfigurationParser.class.getClassLoader().getResourceAsStream("config.json");
            configFile.createNewFile();
            FileWriter writer = new FileWriter(configFile);
            byte[] buffer = new byte[defaultConfig.available()];
            defaultConfig.read(buffer);
            writer.write(new String(buffer));
            writer.close();
            defaultConfig.close();
            throw new ConfigurationParseError("Configuration file does not exist.");
        }

        FileReader reader = new FileReader(configFile);
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            builder.append((char) c);
        }
        reader.close();

        config = new Gson().fromJson(builder.toString(), Configuration.class);

        if (verifyConfiguration(config)) {
            return config;
        }

        return null;
    }

    private static boolean verifyConfiguration(Configuration config) throws ConfigurationParseError {
        if (config.jwtSecret == null || config.jwtSecret.isEmpty()) {
            throw new ConfigurationParseError("JWT secret is not set. This is required and can anything you would like, however it is used to secure user JWT tokens so it should be fairly secure, losing this will not cause any major issues, just invalidate all user tokens.");
        }

        if (config.databaseUrl == null || config.databaseUrl.isEmpty()) {
            throw new ConfigurationParseError("Database URL is not set.");
        }

        if (config.databaseMaxConnections <= 0) {
            throw new ConfigurationParseError("Database max connections is not set.");
        }

        if (config.ip == null || config.ip.isEmpty()) {
            throw new ConfigurationParseError("IP is not set.");
        }

        if (config.port <= 0 || config.port > 65535) {
            throw new ConfigurationParseError("Port is not valid.");
        }

        if (config.maxThreads <= 0) {
            throw new ConfigurationParseError("Max threads is not valid.");
        }

        if (config.maxEndAttempts <= 0) {
            throw new ConfigurationParseError("Max end attempts is not valid.");
        }

        if (config.maxOneshotEndAttempts <= 0) {
            throw new ConfigurationParseError("Max oneshot end attempts is not valid.");
        }
        if (config.threadSaver && config.threadTime<=0) {
            throw new ConfigurationParseError("Thread saver time is not valid.");
        }

        if (config.forceLimit && config.maxLimit <= 0) {
            throw new ConfigurationParseError("Max limit is not valid.");
        }

        if (config.useSSL && (config.keyStoreFile.isEmpty() || config.keyStoreFile.isBlank())) {
            throw new ConfigurationParseError("Key store file is not set.");
        }

        if (config.useSSL && (config.keyStorePassword.isEmpty() || config.keyStorePassword.isBlank())) {
            throw new ConfigurationParseError("Key store password is not set.");
        }

        if (config.useSSL && (config.trustStoreFile.isEmpty() || config.trustStoreFile.isBlank())) {
            throw new ConfigurationParseError("Trust store file is not set.");
        }

        if (config.useSSL && (config.trustStorePassword.isEmpty() || config.trustStorePassword.isBlank())) {
            throw new ConfigurationParseError("Trust store password is not set.");
        }

        return true;
    }

    public static class ConfigurationParseError extends Error {
        public ConfigurationParseError(String message) {
            super(message);
        }
    }

    public static class Configuration {

        public String jwtSecret;

        public String databaseUrl;
        public int databaseMaxConnections;

        public String ip;
        public int port;

        public int maxThreads;
        public int maxEndAttempts;
        public int maxOneshotEndAttempts;

        public boolean forceLimit;
        public int maxLimit;
        public int threadTime;
        public boolean threadSaver;
        public boolean useSSL;
        public String keyStoreFile;
        public String keyStorePassword;
        public String trustStoreFile;
        public String trustStorePassword;
    }

}














