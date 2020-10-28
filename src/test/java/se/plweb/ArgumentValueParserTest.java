package se.plweb;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ArgumentValueParserTest {

    @Test
    public void basicTestSuccess() {

        String[] args = new String[]{"required-files-file=test", "check-folder-for-files=1"};

        ArgumentValueParser argumentValueParser = new ArgumentValueParser(args);
        assertEquals(0, argumentValueParser.getMissingRequiredArguments().size());
    }

    @Test
    public void basicArgumentFailTest() {

        ArgumentValueParser nullArgumentValueParser = new ArgumentValueParser(null);
        assertEquals(0, nullArgumentValueParser.getArgumentValueSet().size());

        ArgumentValueParser nullStringArrayArgumentValueParser = new ArgumentValueParser(new String[]{null});
        assertEquals(0, nullStringArrayArgumentValueParser.getArgumentValueSet().size());

        ArgumentValueParser argumentOnlyArgumentValueParser = new ArgumentValueParser(new String[]{"argument"});
        assertEquals(0, argumentOnlyArgumentValueParser.getArgumentValueSet().size());

        ArgumentValueParser wrongArgumentNameArgumentValueParser = new ArgumentValueParser(new String[]{"null=null"});
        assertEquals(0, wrongArgumentNameArgumentValueParser.getArgumentValueSet().size());
    }

    @Test
    public void basicTest1() {

        String[] args = new String[]{"check-folder-for-files=1"};

        ArgumentValueParser argumentValueParser = new ArgumentValueParser(args);
        assertEquals(1, argumentValueParser.getMissingRequiredArguments().size());
    }

    @Test
    public void suffixTest() {

        String[] args = new String[]{"required-files-file=test", "check-folder-for-files=1", "suffix=.txt"};

        ArgumentValueParser argumentValueParser = new ArgumentValueParser(args);
        List<String> hmm = getValuesForArgument(argumentValueParser.getArgumentValueSet(), Argument.FILE_SUFFIX);

        assertEquals(1, hmm.size());
    }

    @Test
    public void suffixTest2() {

        String[] args = new String[]{"required-files-file=test", "check-folder-for-files=1", "suffix=.txt,.jar"};

        ArgumentValueParser argumentValueParser = new ArgumentValueParser(args);
        List<String> hmm = getValuesForArgument(argumentValueParser.getArgumentValueSet(), Argument.FILE_SUFFIX);

        assertEquals(2, hmm.size());
    }

    private List<String> getValuesForArgument(Set<ArgumentValue> argumentValueSet, Argument argument) {
        return argumentValueSet.stream()
                .filter(argumentValue -> argumentValue.getArgument().equals(argument))
                .map(ArgumentValue::getAllValues)
                .findFirst()
                .orElse(Collections.emptyList());
    }
}
