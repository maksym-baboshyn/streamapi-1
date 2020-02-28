package baboshyn.stramapi;

import org.junit.jupiter.api.DisplayName;
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
                        "assert checking order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("a", "b"), ignoreOrder("c")},
                        "assert ignoring order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "a"), ignoreOrder("c")},
                        "assert ignoring order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "a", "c")},
                        "assert ignore order command blocks"),

                of(asList("a", "b", " c"), new CommandBlock[]{ignoreOrder("a", "b", " c")},
                        "assert ignore order command blocks"),

                of(asList("a\n b\n c", "d"), new CommandBlock[]{ignoreOrder("a", " b", " c", "d")},
                        "assert ignore order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("a", "b")},
                        "assert ignore order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{checkOrder("a"), checkOrder("b", "c")},
                        "assert ignore order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{checkOrder("a"), ignoreOrder("c", "b")},
                        "assert ignore order command blocks"),

                of(asList("a", "b", "c", "d", "e"),
                        new CommandBlock[]{
                                ignoreOrder("b", "a"), ignoreOrder("d", "c"), checkOrder("e")},
                        "assert ignore order command blocks")
        );
    }

    @ParameterizedTest
    @DisplayName("assert commands preserving order.")
    @MethodSource("testDataForPositiveScenarios")
    void assertCommandsPreservingOrder(List<String> actualCommands, CommandBlock[] commandBlocks, String errorCause) {

        assertDoesNotThrow(() -> generator.assertCliCommandsContainBlocks(actualCommands, commandBlocks),
                "Asserting with " + errorCause + " failed.");
    }

    private static Stream<Arguments> testDataForNegativeScenarios() {

        return Stream.of(

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "c"), ignoreOrder("a")},
                        "assert ignoring order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("b", "c")},
                        "assert ignore order command blocks"),

                of(asList("a", "b", "c"), new CommandBlock[]{ignoreOrder("c", "b")},
                        "assert ignore order command blocks"),

                of(singletonList("d"), new CommandBlock[]{ignoreOrder("d ")},
                        "assert ignore order command blocks"),

                of(singletonList("d "), new CommandBlock[]{ignoreOrder("d")},
                        "assert ignore order command blocks"),

                of(asList("a\n b", "c"), new CommandBlock[]{ignoreOrder("a"), ignoreOrder(" b", "c")},
                        "assert ignore order command blocks"),

                of(asList("a", "b\n c"), new CommandBlock[]{checkOrder("a"), ignoreOrder(" c", "b")},
                        "assert ignore order command blocks"),

                of(asList("a", "b"), new CommandBlock[]{ignoreOrder("a", "b", "c")},
                        "assert ignore order command blocks")
        );
    }

    @ParameterizedTest
    @DisplayName("not assert commands, not preserving order")
    @MethodSource("testDataForNegativeScenarios")
    void notAssertCommandsNotPreservingOrder(List<String> actualCommands,
                                             CommandBlock[] commandBlocks,
                                             String errorCause) {

        assertThrows(AssertionError.class,
                () -> generator.assertCliCommandsContainBlocks(actualCommands, commandBlocks),
                "Cannot recognize case when " + errorCause);
    }
}