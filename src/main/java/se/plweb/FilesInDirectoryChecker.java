package se.plweb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class FilesInDirectoryChecker {

    private final static String MISSING_INPUT_ERROR = FilesInDirectoryChecker.class.getSimpleName() + " is missing input";
    private static Log log = LogFactory.getLog(FilesInDirectoryChecker.class);
    private File checkInFolder;
    private Optional<File> compareWithFile;
    private Optional<File> onErrorWriteOutPutToFile;
    private boolean generateCompareFile;
    private String fileSuffix;

    private FilesInDirectoryChecker(Set<ArgumentValue> argumentValues) {
        this();
        for (ArgumentValue argumentValue : argumentValues) {
            switch (argumentValue.getArgument()) {
                case FILE_SUFFIX:
                    this.fileSuffix = argumentValue.getValue();
                    break;
                case ON_ERROR_CREATE_FILE:
                    this.onErrorWriteOutPutToFile = createOptionalFileFrom(argumentValue);
                    break;
                case CHECK_FOLDER_FOR_FILES:
                    this.checkInFolder = new File(argumentValue.getValue());
                    break;
                case REQUIRED_FILES_FILE:
                    this.compareWithFile = createOptionalFileFrom(argumentValue);
                    break;
                case GENERATE_REQUIRED_FILES_FILE:
                    this.generateCompareFile = Boolean.parseBoolean(argumentValue.getValue());
                    break;
            }
        }
    }

    protected FilesInDirectoryChecker() {
        this.compareWithFile = Optional.empty();
        this.onErrorWriteOutPutToFile = Optional.empty();
    }

    public static void main(String[] args) throws MojoFailureException {
        ArgumentValueParser argumentValueHelper = new ArgumentValueParser(args);

        if (argumentValueHelper.isThereMissingRequiredArguments()) {
            throw new MojoFailureException(MISSING_INPUT_ERROR + ":" + argumentValueHelper.getMissingRequiredArguments().toString());
        }

        try {
            FilesInDirectoryChecker.createAndExecute(argumentValueHelper.getArgumentValueSet());
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    public static void createAndExecute(Set<ArgumentValue> argumentValueMap) throws IOException, MojoFailureException {
        new FilesInDirectoryChecker(argumentValueMap).execute();
    }

    private Optional<File> createOptionalFileFrom(ArgumentValue argumentValue) {
        return Optional.of(new File(argumentValue.getValue()));
    }

    public void execute() throws MojoFailureException, IOException {

        checkPreconditions();
        if (generateCompareFile) {
            createOrUpdateFileWithContent(compareWithFile, getFileListAsText());
        } else {
            checkFilesInDirectory();
        }
    }

    private void checkFilesInDirectory() throws MojoFailureException, IOException {

        log.info("Looking for *" + ((fileSuffix != null) ? fileSuffix : "") + " in " + checkInFolder.getAbsolutePath() + " and compering to " + compareWithFile.get().getAbsolutePath());

        if (!getFileListAsText().equals(getCompareFileContents())) {
            createOrUpdateFileWithContent(onErrorWriteOutPutToFile, getFileListAsText()); // generate a file to compare with
            buildAndThrowMojoFailureException();
        } else {
            log.info("OK");
        }
    }

    private void buildAndThrowMojoFailureException() throws MojoFailureException, IOException {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("The list of files is not equal!!!!\n");
        stringBuffer.append(generateDiff(getListOfComparedFileLines(), getListOfFileNamesInCheckInFolder(), compareWithFile.get().getAbsolutePath(), checkInFolder.getAbsolutePath()));
        if (onErrorWriteOutPutToFile.isPresent()) {
            stringBuffer.append("Compare: ");
            stringBuffer.append(compareWithFile.get().getAbsolutePath());
            stringBuffer.append(" with ");
            stringBuffer.append(onErrorWriteOutPutToFile.get().getAbsolutePath() + "\n");
        }

        throw new MojoFailureException(stringBuffer.toString());
    }

    private String getFileListAsText() {
        return convertListOfStringsToString(getListOfFileNamesInCheckInFolder());
    }

    private String getCompareFileContents() throws IOException {
        return convertListOfStringsToString(getListOfComparedFileLines());
    }

    protected void createOrUpdateFileWithContent(Optional<File> optionalFile, String content) throws IOException {
        if (optionalFile.isPresent()) {
            Path path = optionalFile.get().toPath();
            if (content != null && Files.isWritable(path)) {
                write(path, content.getBytes(UTF_8), CREATE, WRITE);
            }
        }
    }

    public String generateDiff(List<String> s1, List<String> s2, String s1Name, String s2Name) {
        StringBuffer stringBuffer = new StringBuffer();
        if (s1 != null && s2 != null) {
            List<String> t1 = new ArrayList<>(s1);
            List<String> t2 = new ArrayList<>(s2);

            if (!t1.containsAll(t2) && !t2.containsAll(t1)) {
                stringBuffer.append(diffLocationMessage(s1Name));
                stringBuffer.append(convertListOfStringsToString(t1));
                stringBuffer.append(diffLocationMessage(s2Name));
                stringBuffer.append(convertListOfStringsToString(t2));
            } else if (t1.size() > t2.size() && t1.removeAll(t2)) {
                stringBuffer.append(diffLocationMessage(s1Name));
                stringBuffer.append(convertListOfStringsToString(t1));
            } else if (t2.size() > t1.size() && t2.removeAll(t1)) {
                stringBuffer.append(diffLocationMessage(s2Name));
                stringBuffer.append(convertListOfStringsToString(t2));
            }
        }

        return stringBuffer.toString();
    }

    private List<String> getListOfFileNamesInCheckInFolder() {

        return Stream.of(Objects.requireNonNull(checkInFolder.listFiles((dir, name) -> {
            if (fileSuffix != null) {
                return name.endsWith(fileSuffix);
            }
            return true;
        }))).map(File::getName).collect(Collectors.toList());
    }

    private List<String> getListOfComparedFileLines() throws IOException {
        return readAllLines(compareWithFile.get().toPath(), UTF_8);
    }

    private String convertListOfStringsToString(List<String> listOfString) {
        if (listOfString == null) return "";

        StringBuffer stringBuffer = new StringBuffer();
        listOfString.sort((s1, s2) -> s1.compareToIgnoreCase(s2));
        listOfString.forEach(line -> stringBuffer.append(line).append("\n"));

        return stringBuffer.toString();
    }

    private void checkPreconditions() {
        if (!checkInFolder.isDirectory() && compareWithFile.isPresent()) {
            throw new RuntimeException(MISSING_INPUT_ERROR);
        }
    }

    protected String diffLocationMessage(String nameOfLocation) {
        return String.format("%s contains:\n", nameOfLocation);
    }
}
