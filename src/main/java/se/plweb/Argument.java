package se.plweb;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum Argument {
    FILE_SUFFIX("suffix", false), REQUIRED_FILES_FILE("required-files-file", true), CHECK_FOLDER_FOR_FILES("check-folder-for" + "-files", true), GENERATE_REQUIRED_FILES_FILE("generate" + "-required-files-file", false), ON_ERROR_CREATE_FILE("on" + "-error-create-file", false);

    private String name;
    private boolean required;

    Argument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public static EnumSet<Argument> getRequiredArguments() {
        return EnumSet.copyOf(Arrays.stream(Argument.values()).filter(Argument::isRequired).collect(Collectors.toSet()));
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return name;
    }
}

