package se.plweb;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ArgumentValueParser {

    private static final String EQUALS_SIGN = "=";
    private static final String COMMA = ",";
    private static final int NAME_AND_VALUE_LENGTH = 2;
    private final Set<ArgumentValue> argumentValueSet;

    ArgumentValueParser(String[] args) {
        argumentValueSet = Arrays.stream(args)
                .map(ArgumentValueParser::argumentValueParser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<ArgumentValue> argumentValueParser(String arg) {
        return Optional.ofNullable(arg)
                .map(ArgumentValueParser::splitToArgumentAndValueArray)
                .filter(ArgumentValueParser::hasNameAndValue)
                .flatMap(argumentAndValue ->
                        findFirstArgumentByNameAndCreateArgumentValue(
                                argumentAndValue[0],
                                argumentAndValue[1]
                        )
                );
    }

    private static String[] splitToArgumentAndValueArray(String argument) {
        return nullSafeSplit(argument, EQUALS_SIGN);
    }

    private static boolean hasNameAndValue(String[] argumentAndValue) {
        return argumentAndValue.length == NAME_AND_VALUE_LENGTH;
    }

    private static Optional<ArgumentValue> findFirstArgumentByNameAndCreateArgumentValue(String name, String value) {
        return findFirstArgumentByName(name).map(argument -> ArgumentValue.create(argument,
                parseValues(value)));
    }

    private static Optional<Argument> findFirstArgumentByName(String name) {
        return Arrays.stream(Argument.values())
                .filter(a -> a.getName().equals(name))
                .findFirst();
    }

    private static List<String> parseValues(String value) {
        return Stream.of(value)
                .filter(Objects::nonNull)
                .map(ArgumentValueParser::splitToValues)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static String[] splitToValues(String value) {
        return nullSafeSplit(value, COMMA);
    }

    private static String[] nullSafeSplit(String value, String regex) {
        if (regex == null) {
            return defaultValueStringArray(value);
        }
        return Optional.ofNullable(value)
                .map(v2 -> v2.split(regex))
                .orElse(defaultValueStringArray(value));
    }

    private static String[] defaultValueStringArray(String value) {
        return new String[]{Optional.ofNullable(value).orElse("")};
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
