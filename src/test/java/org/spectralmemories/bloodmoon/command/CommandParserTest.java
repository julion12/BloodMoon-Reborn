package org.spectralmemories.bloodmoon.command;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    @Test void rejectsNullAndEmptyCommands() {
        assertTrue(CommandParser.parse(null, CommandExecutionMode.SERVER_ONCE).isEmpty());
        assertTrue(CommandParser.parse(" / ", CommandExecutionMode.SERVER_ONCE).isEmpty());
    }

    @Test void stripsLeadingSlash() {
        assertEquals("say hi", CommandParser.parse("/say hi", CommandExecutionMode.SERVER_ONCE).orElseThrow().command());
    }

    @Test void preservesLegacySuffixMeanings() {
        assertEquals(CommandExecutionMode.SERVER_ONCE, CommandParser.parse("say hi;s", CommandExecutionMode.PLAYER_FOR_EACH_PLAYER).orElseThrow().mode());
        assertEquals(CommandExecutionMode.SERVER_FOR_EACH_PLAYER, CommandParser.parse("give $p apple;f", CommandExecutionMode.SERVER_ONCE).orElseThrow().mode());
        assertEquals(CommandExecutionMode.PLAYER_FOR_EACH_PLAYER, CommandParser.parse("me survived;p", CommandExecutionMode.SERVER_ONCE).orElseThrow().mode());
    }

    @Test void placeholdersAreLiteralNullSafeAndUnknownAreRetained() {
        String output = PlaceholderEngine.replace("%player%:%missing%:%null%", Map.of("player", "$1\\name", "null", ""));
        assertEquals("$1\\name:%missing%:", output);
        assertEquals("", PlaceholderEngine.replace(null, Map.of()));
    }
}
