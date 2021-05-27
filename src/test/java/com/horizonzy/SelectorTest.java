package com.horizonzy;

import org.junit.Test;

public class SelectorTest {

    @Test
    public void test() {
        String s = "x=a,y=b,z=c";

        InternalSelector parse = Selector.parse(s);
        System.out.println(111);
    }
}
