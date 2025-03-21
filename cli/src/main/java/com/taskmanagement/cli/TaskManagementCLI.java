package com.taskmanagement.cli;

import com.taskmanagement.cli.command.WelcomeCommand;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class TaskManagementCLI {

    @Autowired
    private WelcomeCommand welcomeCommand;

    public static void main(String[] args) {
        System.setProperty("spring.shell.interactive.enabled", "true");
        System.setProperty("org.jline.terminal.dumb", "false");
        System.setProperty("jansi.passthrough", "true");

        try {
            AnsiConsole.systemInstall();

            SpringApplication app = new SpringApplication(TaskManagementCLI.class);
            app.setBannerMode(Banner.Mode.OFF); // We have our own banner
            app.run(args);
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        welcomeCommand.welcome();
    }
}