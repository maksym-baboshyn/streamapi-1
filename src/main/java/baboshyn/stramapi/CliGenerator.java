package baboshyn.stramapi;

import com.google.common.base.Joiner;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class CliGenerator {
    private final Joiner joiner = Joiner.on("\n");

    /**
     * Checks that {@code commandBlocks} appear in the {@code actualCommands} in the order they were passed.
     * <p>
     * Each individual block of commands can be checked either ignoring the order or preserving it. Sub-commands are always
     * checked preserving the order.
     *
     * @param actualCommands source list of commands
     * @param commandBlocks blocks of commands to appear in the source list
     */
    void assertCliCommandsContainBlocks(List<String> actualCommands, CommandBlock... commandBlocks) {
        List<String> actualCopy = new ArrayList<>(actualCommands);
        for (CommandBlock commandBlock : commandBlocks) {
            final boolean fitsRemaining = commandBlock.commands.size() < actualCopy.size();
            final List<String> subList = actualCopy.subList(0, fitsRemaining ? commandBlock.commands.size() : actualCopy.size());
            final Object[] expectedBlock = joinMainAndSubCommands(commandBlock.commands).toArray();
            final Matcher<Iterable<?>> matcher =
                    commandBlock.checkOrder ? contains(expectedBlock) : containsInAnyOrder(expectedBlock);
            int index = subList.size();

            while (!matcher.matches(joinMainAndSubCommands(subList))) {
                final String nextCommand = actualCopy.get(index);
                subList.remove(0);
                subList.add(nextCommand);
                index++;
                if (index == actualCopy.size()) {
                    // we checked all remaining commands and the are no matches
                    throw new AssertionError("Not matched: " + commandBlock.commands.toString());
                }
            }
            actualCopy = fitsRemaining ? actualCopy.subList(index, actualCopy.size()) : emptyList();
        }
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

    List<String> joinMainAndSubCommands(List<String> commands) {
        final List<String> resultCommands = new ArrayList<>();
        commands.forEach(nextCommand -> {
            if (nextCommand.startsWith(SPACE)) {
                final String prevCmd = resultCommands.isEmpty() ? null : resultCommands.remove(resultCommands.size() - 1);
                if (prevCmd == null) {
                    resultCommands.add(nextCommand);
                } else {
                    resultCommands.add(joiner.join(prevCmd, nextCommand));
                }
            } else {
                resultCommands.add(nextCommand);
            }
        });
        return resultCommands;
    }
}
