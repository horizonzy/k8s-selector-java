package com.horizonzy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class SelectorTest {

    @Test
    public void testSelectorParse() {
        List<String> testGoodStrings = new ArrayList<>();
        testGoodStrings.add("x=a,y=b,z=c");
        testGoodStrings.add("");
        testGoodStrings.add("x!=a,y=b");
        testGoodStrings.add("x=");
        testGoodStrings.add("x= ");
        testGoodStrings.add("x=,z= ");
        testGoodStrings.add("x= ,z= ");
        testGoodStrings.add("!x");
        testGoodStrings.add("x>1");
        testGoodStrings.add("x>1,z<5");

        for (String goodString : testGoodStrings) {
            InternalSelector selector = Selector.parse(goodString);
        }

        List<String> testBadStrings = new ArrayList<>();
        testBadStrings.add("x=a||y=b");
        testBadStrings.add("x==a==b");
        testBadStrings.add("!x=a");
        testBadStrings.add("x<a");

        for (String testBadString : testBadStrings) {
            boolean isException = false;
            try {
                Selector.parse(testBadString);
            } catch (IllegalArgumentException e) {
                isException = true;
            }
            Assert.assertTrue(isException);
        }
    }

    @Test
    public void testDeterministicParse() {
        InternalSelector s1 = Selector.parse("x=a,a=x");

        InternalSelector s2 = Selector.parse("a=x,x=a");

        Assert.assertEquals(s1.toString(), s2.toString());
    }

    @Test
    public void testEveryThing() {
        Map<String, String> label = new HashMap<>();
        label.put("x", "y");
        Assert.assertTrue(Selector.everyThing().matches(label));

        Assert.assertTrue(Selector.everyThing().empty());
    }

    @Test
    public void testSelectorMatches() {
        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "y");
            expectMatch("", label);
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "y");
            expectMatch("x=y", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "y");
            label.put("z", "w");
            expectMatch("x=y,z=w", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "z");
            label.put("z", "a");
            expectMatch("x!=y,z!=w", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("notin", "in");
            expectMatch("notin=in", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "z");
            expectMatch("x", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("y", "z");
            expectMatch("!x", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "2");
            expectMatch("x>1", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "0");
            expectMatch("x<1", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            expectNoMatch("x=z", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "z");
            expectNoMatch("x=y", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "w");
            label.put("z", "w");
            expectNoMatch("x=y,z=w", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "z");
            label.put("z", "w");
            expectNoMatch("x!=y,z!=w", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("y", "z");
            expectNoMatch("x", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "z");
            expectNoMatch("!x", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "0");
            expectNoMatch("x>1", label);
        }

        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "2");
            expectNoMatch("x<1", label);
        }

        Map<String, String> label = new HashMap<>();
        label.put("foo", "bar");
        label.put("baz", "blah");

        expectMatch("foo=bar", label);
        expectMatch("baz=blah", label);
        expectMatch("foo=bar,baz=blah", label);
        expectNoMatch("foo=blah", label);
        expectNoMatch("baz=bar", label);
        expectNoMatch("foo=bar,foobar=bar,baz=blah", label);

    }

    private void expectMatch(String selector, Map<String, String> labels) {
        InternalSelector internalSelector = Selector.parse(selector);
        Assert.assertTrue(internalSelector.matches(labels));
    }

    private void expectNoMatch(String selector, Map<String, String> labels) {
        InternalSelector internalSelector = Selector.parse(selector);
        Assert.assertFalse(internalSelector.matches(labels));

    }

    @Test
    public void testSetMatches() {
        Map<String, String> label = new HashMap<>();
        label.put("foo", "bar");
        label.put("baz", "blah");

        {
            Map<String, String> origin = new HashMap<>();
            expectMatchDirect(origin, label);
        }

        {
            Map<String, String> origin = new HashMap<>();
            origin.put("foo", "bar");
            expectMatchDirect(origin, label);
        }

        {
            Map<String, String> origin = new HashMap<>();
            origin.put("baz", "blah");
            expectMatchDirect(origin, label);
        }

        {
            Map<String, String> origin = new HashMap<>();
            origin.put("foo", "bar");
            origin.put("baz", "blah");
            expectMatchDirect(origin, label);
        }
    }

    private void expectMatchDirect(Map<String, String> selectorMap, Map<String, String> label) {
        Assert.assertTrue(Selector.selectorFromValidatedSet(selectorMap).matches(label));
    }

    @Test
    public void testnullMapIsValid() {
        InternalSelector internalSelector = Selector.selectorFromValidatedSet(null);
        Assert.assertNotNull(internalSelector);
        Assert.assertTrue(internalSelector.empty());
    }

    @Test
    public void testLexer() {
        List<Tuple<String, Integer>> testCases = new ArrayList<>();
        testCases.add(new Tuple<>("", Token.EndOfStringToken));
        testCases.add(new Tuple<>(",", Token.CommaToken));
        testCases.add(new Tuple<>("notin", Token.NotInToken));
        testCases.add(new Tuple<>("in", Token.InToken));
        testCases.add(new Tuple<>("=", Token.EqualsToken));
        testCases.add(new Tuple<>("==", Token.DoubleEqualsToken));
        testCases.add(new Tuple<>(">", Token.GreaterThanToken));
        testCases.add(new Tuple<>("<", Token.LessThanToken));
        testCases.add(new Tuple<>("!", Token.DoesNotExistToken));
        testCases.add(new Tuple<>("!=", Token.NotEqualsToken));
        testCases.add(new Tuple<>("(", Token.OpenParToken));
        testCases.add(new Tuple<>(")", Token.ClosedParToken));
        testCases.add(new Tuple<>("~", Token.IdentifierToken));
        testCases.add(new Tuple<>("||", Token.IdentifierToken));

        for (Tuple<String, Integer> testcase : testCases) {
            Lexer lexer = new Lexer(testcase.getKey());
            Tuple<Integer, String> lexTuple = lexer.lex();
            Assert.assertEquals(lexTuple.getKey(), testcase.getValue());
            Assert.assertFalse(testcase.getValue() != Token.ErrorToken && !testcase.getKey()
                    .equals(lexTuple.getValue()));
        }
    }


    @Test
    public void testLexerSequence() {
        List<Tuple<String, List<Integer>>> testCases = new ArrayList<>();
        testCases.add(new Tuple<>("key in ( value )",
                Arrays.asList(Token.IdentifierToken, Token.InToken, Token.OpenParToken,
                        Token.IdentifierToken, Token.ClosedParToken)));
        testCases.add(new Tuple<>("key notin ( value )",
                Arrays.asList(Token.IdentifierToken, Token.NotInToken, Token.OpenParToken,
                        Token.IdentifierToken, Token.ClosedParToken)));
        testCases.add(new Tuple<>("key in ( value1, value2 )",
                Arrays.asList(Token.IdentifierToken, Token.InToken, Token.OpenParToken,
                        Token.IdentifierToken, Token.CommaToken, Token.IdentifierToken,
                        Token.ClosedParToken)));
        testCases.add(new Tuple<>("key", Arrays.asList(Token.IdentifierToken)));
        testCases.add(new Tuple<>("!key",
                Arrays.asList(Token.DoesNotExistToken, Token.IdentifierToken)));
        testCases.add(new Tuple<>("()", Arrays.asList(Token.OpenParToken, Token.ClosedParToken)));
        testCases.add(new Tuple<>("x in (),y",
                Arrays.asList(Token.IdentifierToken, Token.InToken, Token.OpenParToken,
                        Token.ClosedParToken, Token.CommaToken, Token.IdentifierToken)));
        testCases.add(new Tuple<>("== != (), = notin",
                Arrays.asList(Token.DoubleEqualsToken, Token.NotEqualsToken, Token.OpenParToken,
                        Token.ClosedParToken, Token.CommaToken, Token.EqualsToken,
                        Token.NotInToken)));
        testCases.add(new Tuple<>("key>2",
                Arrays.asList(Token.IdentifierToken, Token.GreaterThanToken,
                        Token.IdentifierToken)));
        testCases.add(new Tuple<>("key<1",
                Arrays.asList(Token.IdentifierToken, Token.LessThanToken, Token.IdentifierToken)));

        for (Tuple<String, List<Integer>> testcase : testCases) {
            List<Integer> tokens = new ArrayList<>();
            Lexer lexer = new Lexer(testcase.getKey());
            for (; ; ) {
                Tuple<Integer, String> lexTuple = lexer.lex();
                if (Token.EndOfStringToken == lexTuple.getKey()) {
                    break;
                }
                tokens.add(lexTuple.getKey());
            }
            Assert.assertEquals(tokens.size(), testcase.getValue().size());

            for (int i = 0; i < Math.min(tokens.size(), testcase.getValue().size()); i++) {
                Assert.assertEquals(tokens.get(i), testcase.getValue().get(i));
            }
        }
    }

    @Test
    public void testParserLookahead() {
        List<Tuple<String, List<Integer>>> testcases = new ArrayList<>();

        testcases.add(new Tuple<>("key in ( value )",
                Arrays.asList(Token.IdentifierToken, Token.InToken, Token.OpenParToken,
                        Token.IdentifierToken, Token.ClosedParToken, Token.EndOfStringToken)));
        testcases.add(new Tuple<>("key notin ( value )",
                Arrays.asList(Token.IdentifierToken, Token.NotInToken, Token.OpenParToken,
                        Token.IdentifierToken, Token.ClosedParToken, Token.EndOfStringToken)));
        testcases.add(new Tuple<>("key in ( value1, value2 )",
                Arrays.asList(Token.IdentifierToken, Token.InToken, Token.OpenParToken,
                        Token.IdentifierToken, Token.CommaToken, Token.IdentifierToken,
                        Token.ClosedParToken, Token.EndOfStringToken)));
        testcases.add(new Tuple<>("key",
                Arrays.asList(Token.IdentifierToken, Token.EndOfStringToken)));
        testcases.add(new Tuple<>("!key",
                Arrays.asList(Token.DoesNotExistToken, Token.IdentifierToken,
                        Token.EndOfStringToken)));
        testcases.add(new Tuple<>("()",
                Arrays.asList(Token.OpenParToken, Token.ClosedParToken, Token.EndOfStringToken)));
        testcases.add(new Tuple<>("", Arrays.asList(Token.EndOfStringToken)));
        testcases.add(new Tuple<>("x in (),y",
                Arrays.asList(Token.IdentifierToken, Token.InToken, Token.OpenParToken,
                        Token.ClosedParToken, Token.CommaToken, Token.IdentifierToken,
                        Token.EndOfStringToken)));
        testcases.add(new Tuple<>("== != (), = notin",
                Arrays.asList(Token.DoubleEqualsToken, Token.NotEqualsToken, Token.OpenParToken,
                        Token.ClosedParToken, Token.CommaToken, Token.EqualsToken, Token.NotInToken,
                        Token.EndOfStringToken)));
        testcases.add(new Tuple<>("key>2",
                Arrays.asList(Token.IdentifierToken, Token.GreaterThanToken, Token.IdentifierToken,
                        Token.EndOfStringToken)));
        testcases.add(new Tuple<>("key<1",
                Arrays.asList(Token.IdentifierToken, Token.LessThanToken, Token.IdentifierToken,
                        Token.EndOfStringToken)));

        for (Tuple<String, List<Integer>> testcase : testcases) {
            Lexer lexer = new Lexer(testcase.getKey());
            Parser parser = new Parser(lexer);
            parser.scan();

            Assert.assertEquals(parser.getScannedItems().size(), testcase.getValue().size());

            for (; ; ) {
                Tuple<Integer, String> aheadTuple = parser
                        .lookAhead(ParseContext.KeyAndOperator);

                Tuple<Integer, String> consumeTuple = parser.consume(ParseContext.KeyAndOperator);

                if (Token.EndOfStringToken == aheadTuple.getKey()) {
                    break;
                }

                Assert.assertEquals(aheadTuple.getKey(), consumeTuple.getKey());
                Assert.assertEquals(aheadTuple.getValue(), consumeTuple.getValue());
            }

        }
    }

    @Test
    public void testParseOperator() {
        List<Tuple<String, Class<? extends Throwable>>> testcases = new ArrayList<>();
        testcases.add(new Tuple<>("in", null));
        testcases.add(new Tuple<>("=", null));
        testcases.add(new Tuple<>("==", null));
        testcases.add(new Tuple<>(">", null));
        testcases.add(new Tuple<>("<", null));
        testcases.add(new Tuple<>("notin", null));
        testcases.add(new Tuple<>("!=", null));
        testcases.add(new Tuple<>("!", IllegalArgumentException.class));
        testcases.add(new Tuple<>("exists", IllegalArgumentException.class));
        testcases.add(new Tuple<>("(", IllegalArgumentException.class));

        for (Tuple<String, Class<? extends Throwable>> testcase : testcases) {
            Lexer lexer = new Lexer(testcase.getKey());
            Parser parser = new Parser(lexer);
            parser.scan();

            boolean isException = false;
            try {
                parser.parseOperator();
            } catch (IllegalArgumentException e) {
                isException = true;
            }
            if (testcase.getValue() == IllegalArgumentException.class) {
                Assert.assertTrue(isException);
            } else {
                Assert.assertFalse(isException);
            }
        }
    }

    @Test
    public void testRequirementConstructor() {
        List<Fourth<String, String, List<String>, Class<? extends Throwable>>> testcases = new ArrayList<>();

        testcases.add(new Fourth<>("x1", Operator.In, null, IllegalArgumentException.class));
        testcases.add(new Fourth<>("x2", Operator.NotIn, Collections.emptyList(),
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x3", Operator.In, Collections.singletonList("foo"), null));
        testcases.add(new Fourth<>("x4", Operator.NotIn, Collections.singletonList("foo"), null));
        testcases.add(new Fourth<>("x5", Operator.Equals, Arrays.asList("foo", "bar"),
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x6", Operator.Exists, null, null));
        testcases.add(new Fourth<>("x7", Operator.DoesNotExist, null, null));
        testcases.add(new Fourth<>("x8", Operator.Exists, Collections.singletonList("foo"),
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x9", Operator.In, Collections.singletonList("bar"), null));
        testcases.add(new Fourth<>("x10", Operator.In, Collections.singletonList("bar"), null));

        testcases.add(new Fourth<>("x11", Operator.GreaterThan, Collections.singletonList("1"),
                null));
        testcases.add(new Fourth<>("x12", Operator.LessThan, Collections.singletonList("6"), null));

        testcases.add(new Fourth<>("x13", Operator.GreaterThan, null,
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x14", Operator.GreaterThan, Collections.singletonList("bar"),
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x15", Operator.LessThan, Collections.singletonList("bar"),
                IllegalArgumentException.class));

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 254; i++) {
            builder.append("a");
        }

        testcases.add(new Fourth<>(builder.toString(), Operator.Exists, null,
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x16", Operator.Equals,
                Collections.singletonList(builder.toString()), IllegalArgumentException.class));
        testcases.add(new Fourth<>("x17", Operator.Equals, Collections.singletonList("a b"),
                IllegalArgumentException.class));
        testcases.add(new Fourth<>("x18", "unsupportedOp", null, IllegalArgumentException.class));

        for (Fourth<String, String, List<String>, Class<? extends Throwable>> testcase : testcases) {
            boolean isException = false;
            try {
                Requirement.newRequirement(testcase.getFirst(), testcase.getSecond(),
                        testcase.getThird());
            } catch (IllegalArgumentException e) {
                isException = true;
            }
            if (testcase.getFourth() == IllegalArgumentException.class) {
                Assert.assertTrue(isException);
            } else {
                Assert.assertFalse(isException);
            }
        }
    }

    @Test
    public void testToString() throws IllegalAccessException, InstantiationException {
        List<Triple<InternalSelector, String, Boolean>> testcases = new ArrayList<>();
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Arrays.asList("abc", "def")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.NotIn, Collections.singletonList("jkl")));
            internalSelector.addRequire(getRequirement("z", Operator.Exists, null));

            testcases.add(new Triple<>(internalSelector, "x in (abc,def),y notin (jkl),z", true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.NotIn, Arrays.asList("abc", "def")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.NotEquals, Collections.singletonList("jkl")));
            internalSelector.addRequire(getRequirement("z", Operator.DoesNotExist, null));
            testcases.add(new Triple<>(internalSelector, "x notin (abc,def),y!=jkl,!z", true));

        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.NotIn, Collections.singletonList("abc")));
            internalSelector
                    .addRequire(getRequirement("y", Operator.In, Arrays.asList("jkl", "mno")));
            internalSelector
                    .addRequire(getRequirement("z", Operator.NotIn, Collections.singletonList("")));
            testcases.add(new Triple<>(internalSelector, "x notin (abc),y in (jkl,mno),z notin ()",
                    true));
        }
        {

            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.Equals, Collections.singletonList("abc")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.DoubleEquals, Collections.singletonList("jkl")));
            internalSelector.addRequire(
                    getRequirement("z", Operator.NotEquals, Collections.singletonList("a")));
            internalSelector.addRequire(getRequirement("z", Operator.Exists, null));
            testcases.add(new Triple<>(internalSelector, "x=abc,y==jkl,z!=a,z", true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.GreaterThan, Collections.singletonList("2")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.LessThan, Collections.singletonList("8")));
            internalSelector.addRequire(getRequirement("z", Operator.Exists, null));
            testcases.add(new Triple<>(internalSelector, "x>2,y<8,z", true));
        }
        for (Triple<InternalSelector, String, Boolean> testcase : testcases) {
            boolean flag = false;
            String out = testcase.getFirst().toString();
            if ("".equals(out) && testcase.getThird()) {
                flag = true;
            } else if (!out.equals(testcase.getSecond())) {
                flag = true;
            }
            Assert.assertFalse(flag);
        }

    }


    private Requirement getRequirement(String key, String operator, List<String> values) {
        return Requirement.newRequirement(key, operator, values);
    }


}
