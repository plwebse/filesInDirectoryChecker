import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestFilesInDirectoryChecker {

    private ArrayList<String> listA;
    private ArrayList<String> listB;
    private FilesInDirectoryChecker checker;

    @Before
    public void setUp() {
        listA = new ArrayList<>();
        listB = new ArrayList<>();
        checker = new FilesInDirectoryChecker();
    }

    @Test
    public void test1() {
        listA.add("a");
        listA.add("b");
        listA.add("c");

        listB.add("a");
        listB.add("b");

        assertEquals(checker.diffLocationMessage("a") + "c\n", checker.generateDiff(listA, listB, "a", "b"));
    }

    @Test
    public void test2() {
        listA.add("a");
        listA.add("b");

        listB.add("a");
        listB.add("b");
        listB.add("c");

        assertEquals(checker.diffLocationMessage("b") + "c\n", checker.generateDiff(listA, listB, "a", "b"));
    }

    @Test
    public void test3() {
        listA.add("a");
        listA.add("b");

        listB.add("a");
        listB.add("b");

        assertEquals("", checker.generateDiff(listA, listB, "a", "b"));
    }
}
