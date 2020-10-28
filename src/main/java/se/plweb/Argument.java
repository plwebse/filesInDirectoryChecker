package se.plweb;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Argument {
    FILE_SUFFIX("suffix", false),
    REQUIRED_FILES_FILE("required-files-file", true),
    CHECK_FOLDER_FOR_FILES("check-folder-for-files", true),
    GENERATE_REQUIRED_FILES_FILE("generate-required-files-file", false),
    ON_ERROR_CREATE_FILE("on-error-create-file", false);

    private final String name;
    private final boolean required;

    Argument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public static EnumSet<Argument> getRequiredArguments() {
        return EnumSet.copyOf(Arrays.
                stream(Argument.values())
                .filter(Argument::isRequired)
                .collect(Collectors.toSet()));
    }

    public static Optional<Argument> findByName(String argumentName) {
        return Arrays.stream(Argument.values())
                .filter(argument -> argument.name.equals(argumentName))
                .findFirst();
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return name;
    }
}

