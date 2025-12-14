package dev.kbd.vekku_server.cli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;

@ShellComponent
public class BrainCommands {

    private final BrainService brainService;

    public BrainCommands(BrainService brainService) {
        this.brainService = brainService;
    }

    @ShellMethod(key = "brain learn", value = "Teach the AI a concept (Tag)")
    public void brainLearn(@ShellOption String tag) {
        brainService.learnTag(tag);
    }
}