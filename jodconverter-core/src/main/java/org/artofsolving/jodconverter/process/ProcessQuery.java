package org.artofsolving.jodconverter.process;

public class ProcessQuery {

    private final String command;
    private final String argument;

    public ProcessQuery(String command, String argument) {
        this.command = command;
        this.argument = argument;
    }

    public String getCommand() {
        return command;
    }

    public String getArgument() {
        return argument;
    }

}
