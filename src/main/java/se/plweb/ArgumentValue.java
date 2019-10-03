package se.plweb;

import java.util.ArrayList;
import java.util.List;

class ArgumentValue {
    private final Argument argument;
    private final List<String> values = new ArrayList<>();

    private ArgumentValue(Argument argument, List<String> values) {
        this.argument = argument;
        this.values.addAll(values);
    }

    static ArgumentValue create(Argument argument, List<String> values) {
        return new ArgumentValue(argument, values);
    }

    Argument getArgument() {
        return argument;
    }

    String getValue() {
        return values
                .stream()
                .findFirst()
                .orElse(null);
    }

    List<String> getValues() {
        return values;
    }
}
