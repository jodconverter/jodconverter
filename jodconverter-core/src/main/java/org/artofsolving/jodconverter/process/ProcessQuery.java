package org.artofsolving.jodconverter.process;

/**
 * Contains the required information used to query for a running process.=
 */
public class ProcessQuery {

    private final String command;
    private final String argument;

    /**
     * Constructs a new instance with the given command and argument.
     * 
     * @param command
     *            the process command.
     * @param argument
     *            the process argument.
     */
    public ProcessQuery(String command, String argument) {
        this.command = command;
        this.argument = argument;
    }

    /**
     * Gets the command of the process to query.
     * 
     * @return the process command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the arguments of the process to query.
     * 
     * @return the process argument.
     */
    public String getArgument() {
        return argument;
    }

}
