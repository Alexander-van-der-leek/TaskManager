package com.taskmanagement.cli.config;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Component
public class UserSession {
    private static final String SESSION_FILE = ".taskmanagement";
    private static final String TOKEN_KEY = "token";
    private static final String USER_NAME_KEY = "userName";
    private static final String USER_EMAIL_KEY = "userEmail";
    private static final String USER_ID_KEY = "userId";
    private static final String USER_ROLE_KEY = "userRole";

    private String token;
    private String userName;
    private String userEmail;
    private String userId;
    private String userRole;

    public UserSession() {
        loadFromFile();
    }

    public boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    // basically just save to user home . and make only owner accesible if possible
    public void saveToFile() {
        Properties properties = new Properties();
        properties.setProperty(TOKEN_KEY, token != null ? token : "");
        properties.setProperty(USER_NAME_KEY, userName != null ? userName : "");
        properties.setProperty(USER_EMAIL_KEY, userEmail != null ? userEmail : "");
        properties.setProperty(USER_ID_KEY, userId != null ? userId : "");
        properties.setProperty(USER_ROLE_KEY, userRole != null ? userRole : "");

        File sessionFile = getSessionFile();
        try (FileOutputStream out = new FileOutputStream(sessionFile)) {
            properties.store(out, "Task Management CLI Session");

            try {
                Path path = sessionFile.toPath();
                if (isPosixCompliant()) {
                    Set<PosixFilePermission> perms = new HashSet<>();
                    perms.add(PosixFilePermission.OWNER_READ);
                    perms.add(PosixFilePermission.OWNER_WRITE);
                    Files.setPosixFilePermissions(path, perms);
                } else {
                    sessionFile.setReadable(false, false);
                    sessionFile.setReadable(true, true);
                    sessionFile.setWritable(false, false);
                    sessionFile.setWritable(true, true);
                    sessionFile.setExecutable(false, false);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not set proper file permissions on session file");
            }
        } catch (IOException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        File sessionFile = getSessionFile();
        if (sessionFile.exists()) {
            Properties properties = new Properties();
            try (FileInputStream in = new FileInputStream(sessionFile)) {
                properties.load(in);
                token = properties.getProperty(TOKEN_KEY);
                userName = properties.getProperty(USER_NAME_KEY);
                userEmail = properties.getProperty(USER_EMAIL_KEY);
                userId = properties.getProperty(USER_ID_KEY);
                userRole = properties.getProperty(USER_ROLE_KEY);
            } catch (IOException e) {
                System.err.println("Error loading session: " + e.getMessage());
                token = null;
                userName = null;
                userEmail = null;
            }
        }
    }

    public void clearSession() {
        token = null;
        userName = null;
        userEmail = null;
        saveToFile();
    }

    private File getSessionFile() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, SESSION_FILE);
    }

    // for e.g max ect.
    private boolean isPosixCompliant() {
        try {
            return Files.getFileAttributeView(Paths.get(System.getProperty("user.home")),
                    java.nio.file.attribute.PosixFileAttributeView.class) != null;
        } catch (Exception e) {
            return false;
        }
    }
}