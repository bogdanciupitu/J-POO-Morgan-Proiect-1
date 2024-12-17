package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

public interface Command {
    /**
     * Executes the command with the given input and output
     *
     * @param command the input command
     * @param output the output of the command
     */
    void execute(CommandInput command, ArrayNode output);
}
