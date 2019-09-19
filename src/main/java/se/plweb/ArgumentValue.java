package se.plweb;

public class ArgumentValue {
    private Argument argument;
    private String value;

    public ArgumentValue(Argument argument, String value) {
        this.argument = argument;
        this.value = value;
    }

    public Argument getArgument() {
        return argument;
    }

    public String getValue() {
        return value;
    }
}
