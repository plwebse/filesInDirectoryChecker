package se.plweb;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestFilesInDirectoryChecker {

    private List<String> listA;
    private List<String> listB;
    private FilesInDirectoryChecker checker;

    @Before
    public void setUp() {
        listA = new ArrayList<>();
        listB = new ArrayList<>();
        checker = new FilesInDirectoryChecker();
    }

    @Test
    public void testDiffLeft() {
        // given
        listA = Arrays.asList("a", "b", "c");
        listB = Arrays.asList("a", "b");

        //when
        String diff = checker.generateDiff(listA, listB, "a", "b");

        //then
        assertEquals(checker.diffLocationMessage("a") + "c\n", diff);
    }

    @Test
    public void testDiffRight() {
        //given
        listA = Arrays.asList("a", "b");
        listB = Arrays.asList("a", "b", "c");

        //when
        String diff = checker.generateDiff(listA, listB, "a", "b");

        //then
        assertEquals(checker.diffLocationMessage("b") + "c\n", diff);
    }

    @Test
    public void testNoDiff() {
        //given
        listA = Arrays.asList("a", "b");
        listB = Arrays.asList("a", "b");

        //when
        String diff = checker.generateDiff(listA, listB, "a", "b");

        //then
        assertEquals("", diff);
    }
}
