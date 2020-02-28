package baboshyn.stramapi;

import com.google.common.base.Joiner;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static baboshyn.stramapi.CliGenerator.MatcherDescription.of;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
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
//        List<String> actualCopy = new ArrayList<>(actualCommands);

        final int[] index = {0};

        final List<String> actualCopy = actualCommands
                .stream()
                .map(command -> Arrays.asList(command.split("\n")))
                .flatMap(Collection::stream)
                .collect(toList());

        System.out.println(actualCopy);

        boolean allMatch = Arrays.stream(commandBlocks)
                .map(commandBlock -> of(convertCommandBlockToMatcher(commandBlock), commandBlock.commands.size()))
                .peek(x -> System.out.println(x.matcher))
                .allMatch(description -> {
                    boolean matches = description.matcher.matches(actualCopy.subList(index[0], index[0] + description.size));
                    index[0] = description.size;
                    return matches;
                });

        System.out.println(allMatch);

        if (!allMatch) {

            throw new AssertionError("Not matched.");
        }

 /*       if (!(isCommandsMatch && actualCopy.isEmpty())) {

            throw new AssertionError("Not matched.");
        }*/

/*        for (CommandBlock commandBlock : commandBlocks) {

            final boolean fitsRemaining = commandBlock.commands.size() < actualCopy.size();

//            final List<String> subList = actualCopy.subList(0, fitsRemaining ? commandBlock.commands.size() : actualCopy.size());

            final long maxSize = fitsRemaining ? commandBlock.commands.size() : actualCopy.size();

            final List<String> subList = actualCopy.stream().limit(maxSize).collect(toList());

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
        }*/
    }

    private Matcher<Iterable<?>> convertCommandBlockToMatcher(CommandBlock block) {

        return block.checkOrder ?
                contains(block.commands.toArray()) :
                containsInAnyOrder(block.commands.toArray());
    }

    static final class MatcherDescription {

        private final Matcher<Iterable<?>> matcher;
        private final int size;

        private MatcherDescription(Matcher<Iterable<?>> matcher, int size) {
            this.matcher = matcher;
            this.size = size;
        }

        public static MatcherDescription of(Matcher<Iterable<?>> matcher, int size) {

            return new MatcherDescription(matcher, size);
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

    /*
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
    */
}
