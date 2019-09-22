package se.plweb;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class ArgumentValueParser {

    private static final String EQUALS_SIGN = "=";
    private static final int NAME_AND_VALUE_LENGTH = 2;
    private final Set<ArgumentValue> argumentValueSet;

    ArgumentValueParser(String[] args) {
        argumentValueSet = Arrays.stream(args)
                .map(ArgumentValueParser::argumentValueParser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<ArgumentValue> findFirstArgumentByNameAndCreateArgumentValue(String name, String value) {
        return findFirstArgumentByName(name).map(argument -> ArgumentValue.create(argument, value));
    }

    private static Optional<ArgumentValue> argumentValueParser(String arg) {
        return Optional.ofNullable(arg)
                .map(ArgumentValueParser::splitToArgumentAndValueArray)
                .filter(ArgumentValueParser::hasNameAndValue).flatMap(argumentAndValue -> findFirstArgumentByNameAndCreateArgumentValue(argumentAndValue[0],
                        argumentAndValue[1]));
    }

    private static Optional<Argument> findFirstArgumentByName(String name) {
        return Arrays.stream(Argument.values())
                .filter(a -> a.getName().equals(name))
                .findFirst();
    }

    private static boolean hasNameAndValue(String[] argumentAndValue) {
        return argumentAndValue.length == NAME_AND_VALUE_LENGTH;
    }

    private static String[] splitToArgumentAndValueArray(String argument1) {
        return argument1.split(EQUALS_SIGN);
    }

    Set<Argument> getMissingRequiredArguments() {
        Set<Argument> parsedArguments =
                argumentValueSet.stream()
                        .map(ArgumentValue::getArgument)
                        .filter(Argument::isRequired)
                        .collect(Collectors.toSet());
        return Argument.getRequiredArguments().stream()
                .filter(argument -> !parsedArguments.contains(argument))
                .collect(Collectors.toSet());
    }

    boolean isThereMissingRequiredArguments() {
        return !getMissingRequiredArguments().isEmpty();
    }

    Set<ArgumentValue> getArgumentValueSet() {
        return argumentValueSet;
    }
}
