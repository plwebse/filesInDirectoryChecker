package se.plweb;

import java.util.*;
import java.util.stream.Collectors;

class ArgumentValueParser {

    private static final String ARGUMENT_AND_VALUE_SEPARATOR = "=";
    private static final String VALUE_SEPARATOR = ",";
    private static final int NAME_AND_VALUE_LENGTH = 2;
    private final Set<ArgumentValue> argumentValueSet;

    ArgumentValueParser(String[] unParsedInputArguments) {
        argumentValueSet = Optional.ofNullable(unParsedInputArguments)
                .map(ArgumentValueParser::parseArgumentValues)
                .orElse(Collections.emptySet());
    }

    private static Set<ArgumentValue> parseArgumentValues(String[] unParsedInputArguments) {
        return Arrays.stream(unParsedInputArguments)
                .map(ArgumentValueParser::argumentValueParser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<ArgumentValue> argumentValueParser(String unParsedInputArgument) {
        return Optional.ofNullable(unParsedInputArgument)
                .map(ArgumentValueParser::splitToArgumentAndValueList)
                .filter(ArgumentValueParser::hasNameAndValue)
                .flatMap(argumentAndValue ->
                        findFirstArgumentByNameAndCreateArgumentValue(argumentAndValue.get(0), argumentAndValue.get(1)));
    }

    private static List<String> splitToArgumentAndValueList(String argument) {
        return nullSafeSplit(argument, ARGUMENT_AND_VALUE_SEPARATOR);
    }

    private static boolean hasNameAndValue(List<String> argumentAndValue) {
        return Optional.ofNullable(argumentAndValue)
                .map(ArgumentValueParser::hasNameAndValueSize)
                .orElse(false);
    }

    private static boolean hasNameAndValueSize(List<String> strings) {
        return NAME_AND_VALUE_LENGTH == strings.size();
    }

    private static Optional<ArgumentValue> findFirstArgumentByNameAndCreateArgumentValue(String name, String unParsedValues) {
        return Argument.findByName(name)
                .map(argument -> ArgumentValue.create(argument, parseValues(unParsedValues)));
    }

    private static List<String> parseValues(String unParsedValues) {
        return nullSafeSplit(unParsedValues, VALUE_SEPARATOR);
    }

    private static List<String> nullSafeSplit(String value, String regex) {
        return Arrays.asList(Optional.ofNullable(value)
                .filter(ArgumentValueParser::isNotBlank)
                .map(v2 -> v2.split(regex))
                .orElse(stringArray(value)));
    }

    private static String[] stringArray(String defaultValue) {
        return new String[]{Optional.ofNullable(defaultValue).orElse("")};
    }

    private static boolean isNotBlank(String s) {
        return Optional.ofNullable(s)
                .map(s1 -> !s1.trim().isEmpty())
                .orElse(false);
    }

    Set<Argument> getMissingRequiredArguments() {
        Set<Argument> parsedArguments = argumentValueSet.stream()
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
