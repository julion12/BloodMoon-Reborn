package org.spectralmemories.bloodmoon.command;

import java.util.Locale;
import java.util.Optional;

/** Parses legacy ;s, ;f and ;p suffixes without changing their meaning. */
public final class CommandParser {
    private CommandParser() { }

    public static Optional<ParsedCommand> parse(String input, CommandExecutionMode defaultMode) {
        if (input == null) return Optional.empty();
        String command = input.trim();
        if (command.startsWith("/")) command = command.substring(1).trim();
        if (command.isEmpty()) return Optional.empty();

        CommandExecutionMode mode = defaultMode;
        int separator = command.lastIndexOf(';');
        if (separator >= 0 && separator == command.length() - 2) {
            String suffix = command.substring(separator + 1).toLowerCase(Locale.ROOT);
            mode = switch (suffix) {
                case "s" -> CommandExecutionMode.SERVER_ONCE;
                case "f" -> CommandExecutionMode.SERVER_FOR_EACH_PLAYER;
                case "p" -> CommandExecutionMode.PLAYER_FOR_EACH_PLAYER;
                default -> mode;
            };
            if (suffix.equals("s") || suffix.equals("f") || suffix.equals("p")) {
                command = command.substring(0, separator).trim();
            }
        }
        return command.isEmpty() ? Optional.empty() : Optional.of(new ParsedCommand(command, mode));
    }
}
