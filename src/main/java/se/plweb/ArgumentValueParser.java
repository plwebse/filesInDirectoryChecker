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
                                argumentAndValue.get(0),
                                argumentAndValue.get(1)
                        )
                );
    }

    private static List<String> splitToArgumentAndValueArray(String argument) {
        return nullSafeSplit(argument, EQUALS_SIGN);
    }

    private static boolean hasNameAndValue(List<String> argumentAndValue) {
        return Optional.ofNullable(argumentAndValue)
                .map(av -> av.size() == NAME_AND_VALUE_LENGTH)
                .orElse(false);
    }

    private static Optional<ArgumentValue> findFirstArgumentByNameAndCreateArgumentValue(String name, String unParsedValues) {
        return Argument.findByName(name)
                .map(argument -> ArgumentValue.create(argument, parseValues(unParsedValues)));
    }

    private static List<String> parseValues(String unParsedValues) {
        return Stream.of(unParsedValues)
                .filter(Objects::nonNull)
                .map(ArgumentValueParser::splitToValues)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static List<String> splitToValues(String value) {
        return nullSafeSplit(value, COMMA);
    }

    private static List<String> nullSafeSplit(String value, String regex) {
        return Arrays.asList(Optional.ofNullable(value)
                .filter(v1 -> isNotBlank(v1, regex))
                .map(v2 -> v2.split(regex))
                .orElse(stringArray(value)));
    }

    private static String[] stringArray(String defaultValue) {
        return new String[]{Optional.ofNullable(defaultValue).orElse("")};
    }

    private static boolean isNotBlank(String... strings) {
        return Optional.ofNullable(strings)
                .map(s1 -> Arrays.stream(s1).allMatch(s -> Objects.nonNull(s) && s.trim().length() > 0))
                .orElse(false);
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
