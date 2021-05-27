package com.horizonzy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SelectorTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();


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
            expectedException.expect(IllegalArgumentException.class);
            InternalSelector selector = Selector.parse(testBadString);
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
    public void testNilMapIsValid() {
        InternalSelector internalSelector = Selector.selectorFromValidatedSet(null);
        Assert.assertNotNull(internalSelector);
        Assert.assertTrue(internalSelector.empty());
    }

    // TODO: 2021/5/28 other unit test complete.


}
