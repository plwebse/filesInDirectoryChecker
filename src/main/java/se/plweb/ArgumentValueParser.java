package se.plweb;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ArgumentValueParser {

    private static final String ARGUMENT_AND_VALUE_SEPARATOR = "=";
    private static final String VALUE_SEPARATOR = ",";
    private static final int NAME_AND_VALUE_LENGTH = 2;
    private static final int NO_LIMIT = 0;
    private final Set<ArgumentValue> argumentValueSet;

    ArgumentValueParser(String[] unParsedInputArguments) {
        argumentValueSet = Optional.ofNullable(unParsedInputArguments)
                .map(ArgumentValueParser::parseArgumentValues)
                .orElse(Collections.emptySet());
    }

    private static Set<ArgumentValue> parseArgumentValues(String[] unParsedInputArguments) {
        return Arrays.stream(unParsedInputArguments)
                .map(ArgumentValueParser::argumentValueParser)
                .flatMap(argumentValue -> argumentValue.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toSet());
    }

    private static Optional<ArgumentValue> argumentValueParser(String unParsedInputArgument) {
        return Optional.ofNullable(unParsedInputArgument)
                .map(ArgumentValueParser::splitToArgumentAndValueList)
                .filter(ArgumentValueParser::hasNameAndValue)
                .flatMap(aav -> findFirstArgumentByNameAndCreateArgumentValue(aav.get(0), aav.get(1)));
    }

    private static List<String> splitToArgumentAndValueList(String argument) {
        return nullSafeSplit(argument, ARGUMENT_AND_VALUE_SEPARATOR, NAME_AND_VALUE_LENGTH);
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
        return nullSafeSplit(unParsedValues, VALUE_SEPARATOR, NO_LIMIT);
    }

    private static List<String> nullSafeSplit(String value, String regex, int limit) {
        return Optional.ofNullable(value)
                .filter(ArgumentValueParser::isNotBlank)
                .map(v1 -> Arrays.asList(v1.split(regex, limit)))
                .orElseGet(() -> Collections.singletonList(value));
    }

    private static boolean isNotBlank(String s) {
        return Optional.ofNullable(s)
                .map(s1 -> !s1.trim().isEmpty())
                .orElse(false);
    }

    protected Set<Argument> getMissingRequiredArguments() {
        Set<Argument> parsedArguments = argumentValueSet.stream()
                .map(ArgumentValue::getArgument)
                .filter(Argument::isRequired)
                .collect(Collectors.toSet());
        return Argument.getRequiredArguments().stream()
                .filter(argument -> !parsedArguments.contains(argument))
                .collect(Collectors.toSet());
    }

    protected boolean isThereMissingRequiredArguments() {
        return !getMissingRequiredArguments().isEmpty();
    }

    protected Set<ArgumentValue> getArgumentValueSet() {
        return argumentValueSet;
    }
}
