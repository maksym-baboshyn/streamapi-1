package baboshyn.stramapi;

import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class CliGenerator {

    /**
     * Checks that {@code commandBlocks} appear in the {@code actualCommands} in the order they were passed.
     * <p>
     * Each individual block of commands can be checked either ignoring the order or preserving it. Sub-commands are always
     * checked preserving the order.
     *
     * @param actualCommands source list of commands
     * @param commandBlocks  blocks of commands to appear in the source list
     */
    void assertCliCommandsContainBlocks(List<String> actualCommands, CommandBlock... commandBlocks) {

        final List<String> actualCommandsFlattened = actualCommands
                .stream()
                .map(command -> asList(command.split("\n")))
                .flatMap(Collection::stream)
                .collect(toList());

        boolean allMatch = stream(commandBlocks)
                .allMatch(commandBlock -> findMatch(actualCommandsFlattened, commandBlock));

        if (!allMatch) {

            throw new AssertionError("Not matched.");
        }
    }

    private boolean findMatch(List<String> actualCommands, CommandBlock providedCommands) {

        String command =
                actualCommands.stream()
                        .filter(providedCommands.commands::contains)
                        .findFirst()
                        .orElseGet(() -> {
                            throw new AssertionError("Not matched.");
                        });

        int commandIndex = actualCommands.indexOf(command);

        List<String> subList = actualCommands.subList(commandIndex, commandIndex + providedCommands.commands.size());

        Matcher<Iterable<?>> matcher = convertCommandBlockToMatcher(providedCommands);

        boolean matches = matcher.matches(subList);

        actualCommands.subList(0, commandIndex + providedCommands.commands.size()).clear();

        return matches;
    }

    private Matcher<Iterable<?>> convertCommandBlockToMatcher(CommandBlock block) {

        return block.checkOrder ?
                contains(block.commands.toArray()) :
                containsInAnyOrder(block.commands.toArray());
    }

    /**
     * Command block means a list of commands which appear in the list close together.
     */
    static final class CommandBlock {

        private final List<String> commands;
        private final boolean checkOrder;

        private CommandBlock(boolean checkOrder, String... commands) {
            this.commands = asList(commands);
            this.checkOrder = checkOrder;
        }

        public static CommandBlock checkOrder(String... commands) {
            return new CommandBlock(true, commands);
        }

        public static CommandBlock ignoreOrder(String... commands) {
            return new CommandBlock(false, commands);
        }
    }
}
