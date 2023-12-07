package org.team5183.beeapi;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.database.Database;
import org.team5183.beeapi.endpoints.ItemEndpoint;
import org.team5183.beeapi.endpoints.MiscEndpoint;
import org.team5183.beeapi.endpoints.UserEndpoint;
import org.team5183.beeapi.entities.UserEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

import static spark.Spark.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        // Parse command line options.
        Options options = null;
        CommandLine cmd = null;
        try {
            options = new Options();

            Option configFile = new Option("c", "config", true, "Configuration file path.");
            configFile.setRequired(false);

            Option generateUser = new Option("g", "no-generate", false, "Do not generate a new administrator user on first run.");
            generateUser.setRequired(false);

            Option defaultUser = new Option("u", "username", true, "Default username for the administrator user.");
            defaultUser.setRequired(false);
            Option defaultPassword = new Option("p", "password", true, "Default password for the administrator user.");
            defaultPassword.setRequired(false);
            Option defaultEmail = new Option("e", "email", true, "Default email for the administrator user.");
            defaultEmail.setRequired(false);
            Option defaultDisplayName = new Option("d", "display-name", true, "Default display name for the administrator user.");
            defaultDisplayName.setRequired(false);

            Option help = new Option("h", "help", false, "Show this page.");
            defaultDisplayName.setRequired(false);

            options.addOption(configFile);

            OptionGroup user = new OptionGroup();
            user.addOption(generateUser);
            user.addOption(defaultUser);
            user.addOption(defaultPassword);
            user.addOption(defaultEmail);
            user.addOption(defaultDisplayName);
            options.addOptionGroup(user);

            options.addOption(help);

            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar beeapi.jar [arguments]", "", options, "Made with ❤️ by Team 5183. https://github.com/frc5183", false);
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar beeapi.jar [arguments]", "", options, "Made with ❤️ by Team 5183. https://github.com/frc5183", false);
            System.exit(0);
        }

        // Parse configuration
        ConfigurationParser.parseConfiguration(cmd.getOptionValue("config", "config.json"));

        // Set port and IP address (environment variables IP & PORT)
        ipAddress(ConfigurationParser.getConfiguration().ip);
        port(ConfigurationParser.getConfiguration().port);

        if (ConfigurationParser.getConfiguration().useSSL)
            secure(ConfigurationParser.getConfiguration().keyStoreFile, ConfigurationParser.getConfiguration().keyStorePassword, ConfigurationParser.getConfiguration().trustStoreFile, ConfigurationParser.getConfiguration().trustStorePassword);

        // todo: fix this as it will cause the program to hang and never finish
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            logger.info("Shutting down...");
//            ThreadingManager.shutdown();
//
//            logger.info("Stopping Spark...");
//            Spark.stop();
//
//            logger.info("Shutdown complete.");
//            System.exit(0);
//        }));


        // Register endpoints
        new ItemEndpoint().registerEndpoints();
        new UserEndpoint().registerEndpoints();
        new MiscEndpoint().registerEndpoints();

        // First run checks
        File init = new File("INITIALIZED");

        if (init.exists() || cmd.hasOption("no-generate")) {
            if (init.canWrite())
                new FileWriter("INITIALIZED").write("NOTE: This file was generated with the --no-generate flag, and did not generate a default admin user, therefore things here may or may not be true.\n!! IF YOU DELETE THIS IT WILL RECREATE A DEFAULT USER !!\nWHICH MAY CAUSE ERRORS WITH DATABASE UNIQUE CONSTRAINTS.\nBefore deleting the database ensure you do one of the following.\n1. Delete the database.\n2. Delete the user.\n3. First run after you delete this file, use the -g parameter to recreate it without creating a new user.\n4. Use the parameters to create another user with a different username and email to avoid constraint issues.");
            logger.warn("Not generating a new administrator user.");
        }
        if (init.createNewFile() && !cmd.hasOption("no-generate")) {
            logger.warn("\n\n--------------------\nFirst run detected, creating a new admin user.");

            String username = cmd.getOptionValue("user", "admin");
            String password = cmd.getOptionValue("password", null);

            if (password == null) {
                // Generate a random password.
                int leftLimit = 40; // (
                int rightLimit = 126; // ~
                SecureRandom random = new SecureRandom();
                StringBuilder buffer = new StringBuilder(14);
                for (int i = 0; i < 14; i++) {
                    int randomLimitedInt = leftLimit + (int)
                            (random.nextFloat() * (rightLimit - leftLimit + 1));
                    buffer.append((char) randomLimitedInt);
                }
                password = buffer.toString();
            }

            String email = cmd.getOptionValue("email", "admin@example.com");
            String displayName = cmd.getOptionValue("display-name", "Administrator");

            try {
                // Create a new admin user.
                Database.getUserDao().create(new UserEntity(username, password, email, displayName, Role.ADMIN));
            } catch (Exception e) {
                e.printStackTrace();
                logger.fatal("Failed to create admin user, exiting.");
                init.delete();
                System.exit(1);
            }

            // Print credentials to console.
            logger.warn("New account\nUsername: admin\nPassword: " + password + "\n--------------------\n\n");
            if (init.canWrite())
                new FileWriter("INITIALIZED").write("!! IF YOU DELETE THIS IT WILL RECREATE A DEFAULT USER !!\nWHICH MAY CAUSE ERRORS WITH DATABASE UNIQUE CONSTRAINTS.\nBefore deleting the database ensure you do one of the following.\n1. Delete the database.\n2. Delete the user.\n3. First run after you delete this file, use the -g parameter to recreate it without creating a new user.\n4. Use the parameters to create another user with a different username and email to avoid constraint issues.");
        }
    }
}
