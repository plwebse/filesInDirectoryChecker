package se.plweb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FilenameFilter;
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

class FilesInDirectoryChecker {

    private final static String MISSING_INPUT_ERROR =
            FilesInDirectoryChecker.class.getSimpleName() + " is missing input";
    private final static Log log = LogFactory.getLog(FilesInDirectoryChecker.class);
    private File checkInFolder;
    private File compareWithFile;
    private File onErrorWriteOutPutToFile;
    private boolean generateCompareFile;
    private List<String> fileSuffix = Collections.emptyList();

    private FilesInDirectoryChecker(Set<ArgumentValue> argumentValues) {
        this();
        for (ArgumentValue argumentValue : argumentValues) {
            switch (argumentValue.getArgument()) {
                case FILE_SUFFIX:
                    this.fileSuffix = argumentValue.getAllValues();
                    break;
                case ON_ERROR_CREATE_FILE:
                    this.onErrorWriteOutPutToFile =
                            new File(argumentValue.getFirstValue());
                    break;
                case CHECK_FOLDER_FOR_FILES:
                    this.checkInFolder = new File(argumentValue.getFirstValue());
                    break;
                case REQUIRED_FILES_FILE:
                    this.compareWithFile =
                            new File(argumentValue.getFirstValue());
                    break;
                case GENERATE_REQUIRED_FILES_FILE:
                    this.generateCompareFile =
                            Boolean.parseBoolean(argumentValue.getFirstValue());
                    break;
            }
        }
    }

    FilesInDirectoryChecker() {
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

    private static void createAndExecute(Set<ArgumentValue> argumentValueMap) throws IOException,
            MojoFailureException {
        new FilesInDirectoryChecker(argumentValueMap).execute();
    }

    private static FilenameFilter filenameFilter(List<String> fileSuffix) {
        return (dir, name) -> {
            if (fileSuffix.isEmpty()) {
                return true;
            } else {
                return fileSuffix.stream().anyMatch(name::endsWith);
            }
        };
    }

    private void execute() throws MojoFailureException, IOException {

        checkPreconditions(generateCompareFile);
        if (!generateCompareFile) {
            checkFilesInDirectory();
        } else {
            createOrUpdateFileWithContent(compareWithFile, getFileListAsText());
        }
    }

    private void checkFilesInDirectory() throws MojoFailureException, IOException {

        log.info(String.format("Looking for %s in %s and compering them to %s",
                Optional.ofNullable(fileSuffix).orElse(Collections.singletonList("")),
                checkInFolder.getAbsolutePath(),
                compareWithFile.getAbsolutePath()));

        if (!getFileListAsText().equals(getCompareFileContents())) {
            createOrUpdateFileWithContent(onErrorWriteOutPutToFile,
                    getFileListAsText()); // generate a file to compare with
            buildAndThrowMojoFailureException();
        } else {
            log.info("OK");
        }
    }

    private void buildAndThrowMojoFailureException() throws MojoFailureException, IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("The list of files is not equal!!!!\n");
        sb.append(generateDiff(getListOfComparedFileLines(),
                getListOfFileNamesInCheckInFolder(),
                compareWithFile.getAbsolutePath(),
                checkInFolder.getAbsolutePath()));
        if (Optional.ofNullable(onErrorWriteOutPutToFile).isPresent()) {
            sb.append("Compare: ");
            sb.append(compareWithFile.getAbsolutePath());
            sb.append(" with ");
            sb.append(onErrorWriteOutPutToFile.getAbsolutePath());
            sb.append("\n");
        }

        throw new MojoFailureException(sb.toString());
    }

    private String getFileListAsText() {
        return convertListOfStringsToString(getListOfFileNamesInCheckInFolder());
    }

    private String getCompareFileContents() throws IOException {
        return convertListOfStringsToString(getListOfComparedFileLines());
    }

    private void createOrUpdateFileWithContent(File file, String content) throws IOException {
        Optional<Path> optionalWritablePath = Optional.ofNullable(file)
                .map(File::toPath)
                .filter(Files::isWritable);

        if (optionalWritablePath.isPresent()) {
            write(optionalWritablePath.get(), content.getBytes(UTF_8), CREATE, WRITE);
        }
    }

    String generateDiff(List<String> listOfFileNames1, List<String> listOfFileNames2,
                        String s1Name, String s2Name) {

        List<String> l1 = createCopy(listOfFileNames1);
        List<String> l2 = createCopy(listOfFileNames2);

        StringBuilder sb = new StringBuilder();
        if (isOneOfTheListsEmptyOrIsThereNowMatchingLines(l1, l2)) {
            sb.append(diffLocationMessage(s1Name));
            sb.append(convertListOfStringsToString(l1));
            sb.append(diffLocationMessage(s2Name));
            sb.append(convertListOfStringsToString(l2));
        } else if (isFirstListBiggerThanSecondListAndRemoveSubsetFromFirstList(l1, l2)) {
            sb.append(diffLocationMessage(s1Name));
            sb.append(convertListOfStringsToString(l1));
        } else if (isFirstListBiggerThanSecondListAndRemoveSubsetFromFirstList(l2, l1)) {
            sb.append(diffLocationMessage(s2Name));
            sb.append(convertListOfStringsToString(l2));
        }

        return sb.toString();
    }

    private boolean isOneOfTheListsEmptyOrIsThereNowMatchingLines(List<String> t1,
                                                                  List<String> t2) {
        return (t1.isEmpty() || t2.isEmpty() || (!t1.containsAll(t2) && !t2.containsAll(t1)));
    }

    private boolean isFirstListBiggerThanSecondListAndRemoveSubsetFromFirstList(List<String> l1,
                                                                                List<String> l2) {
        return l1.size() > l2.size() && l1.removeAll(l2);
    }

    private List<String> createCopy(List<String> listOfStrings) {
        return new ArrayList<>(Optional.ofNullable(listOfStrings).orElse(Collections.emptyList()));
    }

    private List<String> getListOfFileNamesInCheckInFolder() {
        return Stream.of(Objects.requireNonNull(checkInFolder.listFiles(filenameFilter(fileSuffix)))).
                map(File::getName)
                .collect(Collectors.toList());
    }

    private List<String> getListOfComparedFileLines() throws IOException {
        return readAllLines(compareWithFile.toPath(), UTF_8);
    }

    private String convertListOfStringsToString(List<String> listOfString) {
        return Optional.ofNullable(listOfString)
                .orElse(Collections.emptyList())
                .stream().map(line -> String.format("%s\n", line))
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining());
    }

    private void checkPreconditions(boolean shouldGenerateCompareFile) {
        if (checkInFolder.isDirectory() || (!shouldGenerateCompareFile && compareWithFile.isFile())) {
            return;
        }
        throw new RuntimeException(MISSING_INPUT_ERROR);
    }

    protected String diffLocationMessage(String nameOfLocation) {
        return String.format("%s contains:\n", nameOfLocation);
    }
}
