package se.plweb;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ArgumentValueParser {

    private Set<ArgumentValue> argumentValueSet;

    public ArgumentValueParser(String[] args) {
        argumentValueSet = Arrays.stream(args).map(ArgumentValueParser::argumentValueParser).filter(Optional::isPresent).map(Optional::get).
                collect(Collectors.toSet());
    }

    private static Optional<Argument> findFirstArgumentByName(String name) {
        return Arrays.stream(Argument.values()).filter(a -> a.getName().equals(name)).findFirst();
    }

    private static Optional<ArgumentValue> argumentValueParser(String arg) {
        if (arg != null) {
            String[] argumentAndValue = arg.split("=");
            if (argumentAndValue.length == 2) {
                Optional<Argument> optionalArgument = findFirstArgumentByName(argumentAndValue[0]);
                if (optionalArgument.isPresent() && !argumentAndValue[1].isEmpty()) {
                    return Optional.of(new ArgumentValue(optionalArgument.get(), argumentAndValue[1]));
                }
            }
        }

        return Optional.empty();
    }

    public Set<Argument> getMissingRequiredArguments() {
        Set<Argument> parsedArguments = argumentValueSet.stream().map(ArgumentValue::getArgument).filter(Argument::isRequired).collect(Collectors.toSet());
        return Argument.getRequiredArguments().stream().filter(argument -> !parsedArguments.contains(argument)).collect(Collectors.toSet());
    }

    public boolean isThereMissingRequiredArguments() {
        return !getMissingRequiredArguments().isEmpty();
    }

    public Set<ArgumentValue> getArgumentValueSet() {
        return argumentValueSet;
    }
}
