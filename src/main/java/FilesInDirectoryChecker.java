import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class FilesInDirectoryChecker {

    private File checkInFolder = null;
    private File compareWithFile = null;
    private File onErrorWriteOutPutToFile = null;
    private boolean generateCompareFile = false;
    private String fileSuffix = null;

    private static Log log = LogFactory.getLog(FilesInDirectoryChecker.class);

    private final static String MISSING_INPUT_ERROR = FilesInDirectoryChecker.class.getSimpleName() + " is missing input";

    /**
     * @param fileSuffix
     * @param checkInFolder
     * @param compareWithFile
     * @param onErrorWriteOutPutToFile
     * @param generateCompareFile
     */
    public static void createAndExecute(String fileSuffix, String checkInFolder, String compareWithFile, String onErrorWriteOutPutToFile, boolean generateCompareFile) throws IOException, MojoFailureException {
        FilesInDirectoryChecker filesInDirectoryChecker = new FilesInDirectoryChecker(fileSuffix, checkInFolder, compareWithFile, onErrorWriteOutPutToFile, generateCompareFile);
        filesInDirectoryChecker.execute();
    }

    private FilesInDirectoryChecker(String fileSuffix, String checkInFolder, String compareWithFile, String onErrorWriteOutPutToFile, boolean generateCompareFile) {
        this.fileSuffix = fileSuffix;
        this.checkInFolder = new File(checkInFolder);
        this.compareWithFile = new File(compareWithFile);
        this.onErrorWriteOutPutToFile = new File(onErrorWriteOutPutToFile);
        this.generateCompareFile = generateCompareFile;
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

        log.info("Looking for *" + fileSuffix + " in " + checkInFolder.getAbsolutePath() + " and compering to " + compareWithFile.getAbsolutePath());

        if (!getFileListAsText().equals(getCompareFileContents())) {
            createOrUpdateFileWithContent(onErrorWriteOutPutToFile, getFileListAsText()); // generate a file to compare with
            throw new MojoFailureException("The list of files is not equal!!!!\n" + generateDiff(getListOfComparedFileLines(), getListOfFileNamesInCheckInFolder(), compareWithFile.getAbsolutePath(), checkInFolder.getAbsolutePath()) + "Compare: " + compareWithFile.getAbsolutePath() + " with " + onErrorWriteOutPutToFile.getAbsolutePath() + "\n");
        } else {
            log.info("OK");
        }
    }

    private String getFileListAsText() {
        return convertListOfStringsToString(getListOfFileNamesInCheckInFolder());
    }

    private String getCompareFileContents() throws IOException {
        return convertListOfStringsToString(getListOfComparedFileLines());
    }

    protected void createOrUpdateFileWithContent(File filePath, String content) throws IOException {
        if (content != null && filePath != null && !filePath.isDirectory()) {
            write(filePath.toPath(), content.getBytes(UTF_8), CREATE, WRITE);
        }
    }

    public String generateDiff(List<String> s1, List<String> s2, String s1Name, String s2Name) {
        StringBuffer stringBuffer = new StringBuffer();
        if (s1 != null && s2 != null) {
            List<String> t1 = new ArrayList<>(s1);
            List<String> t2 = new ArrayList<>(s2);

            if (t1.size() > t2.size() && t1.removeAll(t2)) {
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
        return Stream.of(Objects.requireNonNull(checkInFolder.listFiles((dir, name) -> name.endsWith(fileSuffix)))).map(File::getName).collect(Collectors.toList());
    }

    private List<String> getListOfComparedFileLines() throws IOException {
        return readAllLines(compareWithFile.toPath(), UTF_8);
    }

    private String convertListOfStringsToString(List<String> listOfString) {
        if (listOfString == null) return "";

        StringBuffer stringBuffer = new StringBuffer();
        listOfString.sort((s1, s2) -> s1.compareToIgnoreCase(s2));
        listOfString.forEach(line -> {
            stringBuffer.append(line).append("\n");
        });

        return stringBuffer.toString();
    }

    private void checkPreconditions() {
        if (!checkInFolder.isDirectory() && fileSuffix != null && fileSuffix.length() > 0 && compareWithFile != null && onErrorWriteOutPutToFile != null) {
            throw new RuntimeException(MISSING_INPUT_ERROR);
        }
    }

    protected String diffLocationMessage(String nameOfLocation) {
        return String.format("%s contains:\n", nameOfLocation);
    }

    protected FilesInDirectoryChecker() {
    }

    /**
     * @param args
     * @throws MojoFailureException args[0] file suffix to check
     *                              args[1] path to directory to check
     *                              args[2] path to file with content compare with directory
     *                              args[3] path to file where to write current files in
     *                              args[4] generate content to compare in file referenced in arguments[2]
     */

    public static void main(String[] args) throws MojoFailureException {
        if (args.length < 4) throw new MojoFailureException(MISSING_INPUT_ERROR);
        boolean generateContent = (args.length == 5) ? Boolean.parseBoolean(args[4]) : false;

        try {
            FilesInDirectoryChecker.createAndExecute(args[0], args[1], args[2], args[3], generateContent);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
