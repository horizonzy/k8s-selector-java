package com.horizonzy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    public void testToString()
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
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
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Arrays.asList("abc", "def")));
            internalSelector
                    .addRequire(getEmptyRequirement()); // adding empty req for the trailing ','
            testcases.add(new Triple<>(internalSelector, "x in (abc,def),", false));
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

    private Requirement getEmptyRequirement()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Requirement> constructor = Requirement.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    @Test
    public void testRequirementSelectorMatching()
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<Triple<Map<String, String>, InternalSelector, Boolean>> testcases = new ArrayList<>();
        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "foo");
            label.put("y", "baz");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getEmptyRequirement());
            testcases.add(new Triple<>(label, internalSelector, false));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "foo");
            label.put("y", "baz");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Collections.singletonList("foo")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.NotIn, Collections.singletonList("alpha")));
            testcases.add(new Triple<>(label, internalSelector, true));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("x", "foo");
            label.put("y", "baz");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Collections.singletonList("foo")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.In, Collections.singletonList("alpha")));
            testcases.add(new Triple<>(label, internalSelector, false));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("y", "");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.NotIn, Collections.singletonList("")));
            internalSelector.addRequire(getRequirement("y", Operator.Exists, null));
            testcases.add(new Triple<>(label, internalSelector, true));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("y", "");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("x", Operator.DoesNotExist, null));
            internalSelector.addRequire(getRequirement("y", Operator.Exists, null));
            testcases.add(new Triple<>(label, internalSelector, true));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("y", "");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.NotIn, Collections.singletonList("")));
            internalSelector.addRequire(getRequirement("y", Operator.DoesNotExist, null));
            testcases.add(new Triple<>(label, internalSelector, false));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("y", "baz");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Collections.singletonList("")));
            testcases.add(new Triple<>(label, internalSelector, false));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("z", "2");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("z", Operator.GreaterThan, Collections.singletonList("1")));
            testcases.add(new Triple<>(label, internalSelector, true));
        }
        {
            Map<String, String> label = new HashMap<>();
            label.put("z", "v2");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("z", Operator.GreaterThan, Collections.singletonList("1")));
            testcases.add(new Triple<>(label, internalSelector, false));
        }

        for (Triple<Map<String, String>, InternalSelector, Boolean> testcase : testcases) {
            Assert.assertEquals(testcase.getSecond().matches(testcase.getFirst()),
                    testcase.getThird());
        }
    }

    @Test
    public void testSetSelectorParser() {
        List<Fourth<String, InternalSelector, Boolean, Boolean>> testcases = new ArrayList<>();
        {
            testcases.add(new Fourth<>("", new InternalSelector(), true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("x", Operator.Exists, null));
            testcases.add(new Fourth<>("\rx", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("this-is-a-dns.domain.com/key-with-dash", Operator.Exists,
                            null));
            testcases.add(new Fourth<>("this-is-a-dns.domain.com/key-with-dash", internalSelector,
                    true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("this-is-another-dns.domain.com/key-with-dash", Operator.In,
                            Arrays.asList("so", "what")));
            testcases.add(new Fourth<>("this-is-another-dns.domain.com/key-with-dash in (so,what)",
                    internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("0.1.2.domain/99", Operator.NotIn,
                    Arrays.asList("10.10.100.1", "tick.tack.clock")));
            testcases.add(new Fourth<>("0.1.2.domain/99 notin (10.10.100.1, tick.tack.clock)",
                    internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("foo", Operator.In, Collections.singletonList("abc")));
            testcases.add(new Fourth<>("foo  in	 (abc)", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.NotIn, Collections.singletonList("abc")));
            testcases.add(new Fourth<>("x notin\n (abc)", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.NotIn, Arrays.asList("abc", "def")));
            testcases
                    .add(new Fourth<>("x  notin	\t	(abc,def)", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Arrays.asList("abc", "def")));
            testcases.add(new Fourth<>("x in (abc,def)", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("x", Operator.In, Arrays.asList("abc", "")));
            testcases.add(new Fourth<>("x in (abc,)", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.In, Collections.singletonList("")));
            testcases.add(new Fourth<>("x in ()", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("bar", Operator.Exists, null));
            internalSelector.addRequire(getRequirement("w", Operator.Exists, null));
            internalSelector.addRequire(
                    getRequirement("x", Operator.NotIn, Arrays.asList("abc", "", "def")));
            internalSelector
                    .addRequire(getRequirement("z", Operator.In, Collections.singletonList("")));
            testcases.add(new Fourth<>("x notin (abc,,def),bar,z in (),w", internalSelector, true,
                    true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("y", Operator.In, Collections.singletonList("a")));
            internalSelector.addRequire(getRequirement("x", Operator.Exists, null));
            testcases.add(new Fourth<>("x,y in (a)", internalSelector, false, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.Equals, Collections.singletonList("a")));
            testcases.add(new Fourth<>("x=a", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.GreaterThan, Collections.singletonList("1")));
            testcases.add(new Fourth<>("x>1", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.LessThan, Collections.singletonList("7")));
            testcases.add(new Fourth<>("x<7", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.Equals, Collections.singletonList("a")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.NotEquals, Collections.singletonList("b")));
            testcases.add(new Fourth<>("x=a,y!=b", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("x", Operator.Equals, Collections.singletonList("a")));
            internalSelector.addRequire(
                    getRequirement("y", Operator.NotEquals, Collections.singletonList("b")));
            internalSelector
                    .addRequire(getRequirement("z", Operator.In, Arrays.asList("h", "i", "j")));
            testcases.add(new Fourth<>("x=a,y!=b,z in (h,i,j)", internalSelector, true, true));
        }
        {
            testcases.add(new Fourth<>("x=a||y=b", new InternalSelector(), false, false));
        }
        {
            testcases.add(new Fourth<>("x,,y", null, true, false));
        }
        {
            testcases.add(new Fourth<>(",x,y", null, true, false));
        }
        {
            testcases.add(new Fourth<>("x nott in (y)", null, true, false));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.NotIn, Collections.singletonList("")));
            testcases.add(new Fourth<>("x notin ( )", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector
                    .addRequire(getRequirement("x", Operator.NotIn, Arrays.asList("", "a")));
            testcases.add(new Fourth<>("x notin (, a)", internalSelector, true, true));
        }
        {
            testcases.add(new Fourth<>("a in (xyz),", null, true, false));
        }
        {
            testcases.add(new Fourth<>("a in (xyz)b notin ()", null, true, false));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("a", Operator.Exists, null));
            testcases.add(new Fourth<>("a ", internalSelector, true, true));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("a", Operator.In, Arrays.asList("in", "notin", "x", "y", "z")));
            testcases.add(new Fourth<>("a in (x,y,notin, z,in)", internalSelector, true, true));
        }
        {
            testcases.add(new Fourth<>("a in (xyz abc)", null, false, false));
        }
        {
            testcases.add(new Fourth<>("a notin(", null, true, false));
        }
        {
            testcases.add(new Fourth<>("a (", null, false, false));
        }
        {
            testcases.add(new Fourth<>("(", null, false, false));
        }

        for (Fourth<String, InternalSelector, Boolean, Boolean> testcase : testcases) {
            boolean isException = false;
            InternalSelector selector = null;
            try {
                selector = Selector.parse(testcase.getFirst());
            } catch (IllegalArgumentException e) {
                isException = true;
            }
            if (isException) {
                Assert.assertFalse(testcase.getFourth());
            } else {
                Assert.assertTrue(testcase.getFourth());
                if (testcase.getThird()) {
                    Assert.assertEquals(selector.toString(), testcase.getSecond().toString());
                }
            }
        }
    }

    private static class AddTestEntity {

        private String name;
        private InternalSelector selector;
        private String key;
        private String operator;
        private List<String> values;
        private InternalSelector refSelector;

        public AddTestEntity(String name, InternalSelector selector, String key, String operator,
                List<String> values, InternalSelector refSelector) {
            this.name = name;
            this.selector = selector;
            this.key = key;
            this.operator = operator;
            this.values = values;
            this.refSelector = refSelector;
        }
    }

    @Test
    public void testAdd() {
        List<AddTestEntity> testcases = new ArrayList<>();

        {
            InternalSelector selector = new InternalSelector();

            InternalSelector refSelector = new InternalSelector();
            refSelector.addRequire(
                    getRequirement("key", Operator.In, Collections.singletonList("value")));

            testcases.add(new AddTestEntity("keyInOperator", selector, "key", Operator.In,
                    Collections.singletonList("value"), refSelector));
        }
        {
            InternalSelector selector = new InternalSelector();
            selector.addRequire(
                    getRequirement("key", Operator.In, Collections.singletonList("value")));

            InternalSelector refSelector = new InternalSelector();
            refSelector.addRequire(
                    getRequirement("key", Operator.In, Collections.singletonList("value")));
            refSelector.addRequire(
                    getRequirement("key2", Operator.Equals, Collections.singletonList("value2")));

            testcases.add(new AddTestEntity("keyEqualsOperator", selector, "key2", Operator.Equals,
                    Collections.singletonList("value2"), refSelector));
        }
        for (AddTestEntity testcase : testcases) {
            Requirement requirement = Requirement
                    .newRequirement(testcase.key, testcase.operator, testcase.values);
            testcase.selector.addRequire(requirement);

            Assert.assertEquals(testcase.selector.toString(), testcase.refSelector.toString());
        }
    }

    @Test
    public void testSafeSort() {
        List<Fourth<String, List<String>, List<String>, List<String>>> testcases = new ArrayList<>();
        {
            testcases.add(new Fourth<>("nil strings", null, null, null));
        }
        {
            testcases.add(new Fourth<>("ordered strings", Arrays.asList("bar", "foo"),
                    Arrays.asList("bar", "foo"), Arrays.asList("bar", "foo")));
        }
        {
            testcases.add(new Fourth<>("unordered strings", Arrays.asList("foo", "bar"),
                    Arrays.asList("foo", "bar"), Arrays.asList("bar", "foo")));
        }
        {
            testcases.add(new Fourth<>("duplicated strings",
                    Arrays.asList("foo", "bar", "foo", "bar"),
                    Arrays.asList("foo", "bar", "foo", "bar"),
                    Arrays.asList("bar", "bar", "foo", "foo")));
        }

        for (Fourth<String, List<String>, List<String>, List<String>> testcase : testcases) {
            List<String> got = Selector.safeSort(testcase.getSecond());
            Assert.assertEquals(got, testcase.getFourth());
            Assert.assertEquals(testcase.getSecond(), testcase.getThird());
        }
    }

    @Test
    public void benchmarkSelectorFromValidatedSet() {
        Map<String, String> data = new HashMap<>();
        data.put("foo", "foo");
        data.put("bar", "bar");

        for (int i = 0; i < 10000; i++) {
            Selector.selectorFromValidatedSet(data);
        }
    }

    @Test
    public void testRequiresExactMatch() {
        List<Fourth<InternalSelector, String, Boolean, String>> testcases = new ArrayList<>();
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.In, Collections.singletonList("value")));
            testcases.add(new Fourth<>(internalSelector, "key", true, "value"));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.In, Arrays.asList("value", "value2")));
            testcases.add(new Fourth<>(internalSelector, "key", false, "value"));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.In, Arrays.asList("value", "value1")));
            internalSelector.addRequire(
                    getRequirement("key2", Operator.In, Collections.singletonList("value2")));
            testcases.add(new Fourth<>(internalSelector, "key2", true, "value2"));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.Equals, Collections.singletonList("value")));
            testcases.add(new Fourth<>(internalSelector, "key", true, "value"));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(getRequirement("key", Operator.DoubleEquals,
                    Collections.singletonList("value")));
            testcases.add(new Fourth<>(internalSelector, "key", true, "value"));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.NotEquals, Collections.singletonList("value")));
            testcases.add(new Fourth<>(internalSelector, "key", false, ""));
        }
        {
            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.In, Collections.singletonList("value")));
            internalSelector.addRequire(
                    getRequirement("key2", Operator.In, Collections.singletonList("value2")));
            testcases.add(new Fourth<>(internalSelector, "key", true, "value"));
        }
        for (Fourth<InternalSelector, String, Boolean, String> testcase : testcases) {
            Tuple<String, Boolean> tuple = testcase.getFirst()
                    .requiresExactMatch(testcase.getSecond());
            Assert.assertEquals(tuple.getValue(), testcase.getThird());
            if (tuple.getValue()) {
                Assert.assertEquals(tuple.getKey(), testcase.getFourth());
            }
        }
    }

    @Test
    public void testValidatedSelectorFromSet() {
        List<Triple<Map<String, String>, InternalSelector, Class<? extends Throwable>>> testcases = new ArrayList<>();
        {
            Map<String, String> input = new HashMap<>();
            input.put("key", "val");

            InternalSelector internalSelector = new InternalSelector();
            internalSelector.addRequire(
                    getRequirement("key", Operator.Equals, Collections.singletonList("val")));
            testcases.add(new Triple<>(input, internalSelector, null));
        }
        {
            Map<String, String> input = new HashMap<>();
            input.put("Key", "axahm2EJ8Phiephe2eixohbee9eGeiyees1thuozi1xoh0GiuH3diewi8iem7Nui");

            testcases.add(new Triple<>(input, null, IllegalArgumentException.class));
        }

        for (Triple<Map<String, String>, InternalSelector, Class<? extends Throwable>> testcase : testcases) {
            InternalSelector internalSelector = null;
            boolean isException = false;
            try {
                internalSelector = Selector.selectorFromValidatedSet(testcase.getFirst());
            } catch (IllegalArgumentException e) {
                isException = true;
            }
            if (isException) {
                Assert.assertEquals(testcase.getThird(), IllegalArgumentException.class);
            } else {
                Assert.assertEquals(internalSelector.toString(), testcase.getSecond().toString());
            }
        }
    }

    @Test
    public void testRequirementEqual() {
        List<Triple<Requirement, Requirement, Boolean>> testcases = new ArrayList<>();
        {
            Requirement oneRequirement = Requirement
                    .newRequirement("key", Operator.In, Arrays.asList("foo", "bar"));

            Requirement secondRequirement = Requirement
                    .newRequirement("key", Operator.In, Arrays.asList("foo", "bar"));

            testcases.add(new Triple<>(oneRequirement, secondRequirement, true));
        }
        {
            Requirement oneRequirement = Requirement
                    .newRequirement("key1", Operator.In, Arrays.asList("foo", "bar"));

            Requirement secondRequirement = Requirement
                    .newRequirement("key2", Operator.In, Arrays.asList("foo", "bar"));

            testcases.add(new Triple<>(oneRequirement, secondRequirement, false));
        }
        {
            Requirement oneRequirement = Requirement
                    .newRequirement("key", Operator.In, Arrays.asList("foo", "bar"));
            Requirement secondRequirement = Requirement
                    .newRequirement("key", Operator.NotIn, Arrays.asList("foo", "bar"));
            testcases.add(new Triple<>(oneRequirement, secondRequirement, false));
        }
        {
            Requirement oneRequirement = Requirement
                    .newRequirement("key", Operator.In, Arrays.asList("foo", "bar"));
            Requirement secondRequirement = Requirement
                    .newRequirement("key", Operator.In, Collections.singletonList("foobar"));
            testcases.add(new Triple<>(oneRequirement, secondRequirement, false));
        }

        for (Triple<Requirement, Requirement, Boolean> testcase : testcases) {
            Assert.assertEquals(testcase.getFirst().toString().equals(testcase.getSecond().toString()), testcase.getThird());
        }
    }

}
