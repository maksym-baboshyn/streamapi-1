package baboshyn.stramapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static baboshyn.stramapi.CliGenerator.CommandBlock;
import static baboshyn.stramapi.CliGenerator.CommandBlock.checkOrder;
import static baboshyn.stramapi.CliGenerator.CommandBlock.ignoreOrder;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Cli Generator should ")
class CliGeneratorTest {

    private final CliGenerator generator = new CliGenerator();

    private static Stream<Arguments> testDataForPositiveScenarios() {

        return Stream.of(

                of(asList("a", "b", "c"), new CommandBlock[]{checkOrder("a", "b"), checkOrder("c")},
                        "2 checking order blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("a", "b"), ignoreOrder("c")},
                        "2 ignoring order blocks, commands of each going in right order"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "a"), ignoreOrder("c")},
                        "2 ignoring order blocks, commands of each going in wrong order"),

                of(asList("a", "b"), new CommandBlock[]{ignoreOrder("b", "a")},
                        "1 ignoring order block, with size matching to the size fo actual block"),

                of(singletonList("a\n b"), new CommandBlock[]{ignoreOrder("a", " b")},
                        "command with space in the middle of command block"),

                of(asList("a\n b\n c", "d"), new CommandBlock[]{ignoreOrder("a", " b", " c", "d")},
                        "multiple commands with spaces"),

                of(asList("a\n b", "c"), new CommandBlock[]{ignoreOrder("a"), ignoreOrder(" b", "c")},
                        "command with space in the beginning of second block"),

                of(asList("a", "b\n c"), new CommandBlock[]{checkOrder("a"), ignoreOrder(" c", "b")},
                        "command both having space in the beginning of second block and ignoring order"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("a", "b")},
                        "command block included in actual block of commands"),

                of(asList("a", "b", "c"), new CommandBlock[]{checkOrder("a"), ignoreOrder("c", "b")},
                        "mixed types of commands blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("c", "b")},
                        "case when first command missing and order ignored"),

                of(asList("a", "b", "c", "d", "e", "f\n g", "k"),
                        new CommandBlock[]{
                                ignoreOrder("b", "a"),
                                ignoreOrder("d", "c"),
                                checkOrder("e"),
                                ignoreOrder("k", "f", " g")},
                        "multiple blocks ignoring order"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "c")},
                        "case when first command missing and order is checked"),

                of(asList("a", "b", "c", "d"), new CommandBlock[]{ignoreOrder("b", "c"), ignoreOrder("d")},
                        "case with missing command and divided last commands"),

                of(asList("a", "b", "c", "d"), new CommandBlock[]{ignoreOrder("b"), ignoreOrder("d")},
                        "case when command missing through one"),

                of(asList("a", "b", "c", "d"), new CommandBlock[]{ignoreOrder("b", "c", "d")},
                        "case with missing command and joined last commands"),

                of(asList("\n a", "b"), new CommandBlock[]{ignoreOrder(" a", "b")},
                        "next-line (joiner) character found in the beginning of command line.")
        );
    }

    @ParameterizedTest
    @DisplayName("assert commands preserving order.")
    @MethodSource("testDataForPositiveScenarios")
    void testCommandsPreservingOrder(List<String> actualCommands, CommandBlock[] commandBlocks, String errorCause) {

        assertDoesNotThrow(() -> generator.assertCliCommandsContainBlocks(actualCommands, commandBlocks),
                "Asserting of " + errorCause + " failed.");
    }

    private static Stream<Arguments> testDataForNegativeScenarios() {

        return Stream.of(

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "c"), ignoreOrder("a")},
                        "command blocks go in wrong order."),

                of(singletonList("d"), new CommandBlock[]{ignoreOrder("d ")},
                        "command ends with space"),

                of(singletonList("d "), new CommandBlock[]{ignoreOrder("d")},
                        "actual command ends with space")
        );
    }

    @ParameterizedTest
    @DisplayName("not assert commands, not preserving order")
    @MethodSource("testDataForNegativeScenarios")
    void testCommandsNotPreservingOrder(List<String> actualCommands, CommandBlock[] commandBlocks, String errorCause) {

        assertThrows(AssertionError.class,
                () -> generator.assertCliCommandsContainBlocks(actualCommands, commandBlocks),
                "Cannot recognize case when " + errorCause);
    }

    @Test
    @DisplayName("not assert lesser number of actual commands")
    void testDifferentNumberOfCommands() {

        assertThrows(IndexOutOfBoundsException.class,
                () -> generator.assertCliCommandsContainBlocks(asList("a", "b"), ignoreOrder("a", "b", "c")),
                "Cannot say that number of actual commands less than number of provided commands.");
    }
}