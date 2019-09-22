package se.plweb;

class ArgumentValue {
    private final Argument argument;
    private final String value;

    private ArgumentValue(Argument argument, String value) {
        this.argument = argument;
        this.value = value;
    }

    static ArgumentValue create(Argument argument, String value) {
        return new ArgumentValue(argument, value);
    }

    Argument getArgument() {
        return argument;
    }

    String getValue() {
        return value;
    }
}
