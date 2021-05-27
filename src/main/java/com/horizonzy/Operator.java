package com.horizonzy;

import java.util.Arrays;
import java.util.List;

public class Operator {

    public static final String DoesNotExist = "!";

    public static final String Equals = "=";

    public static final String DoubleEquals = "==";

    public static final String In = "in";

    public static final String NotEquals = "!=";

    public static final String NotIn = "notin";

    public static final String Exists = "exists";

    public static final String GreaterThan = "gt";

    public static final String LessThan = "lt";

    public static List<String> unaryOperators = Arrays.asList(Exists, DoesNotExist);

    public static List<String> binaryOperators = Arrays
            .asList(In, NotIn, Equals, DoubleEquals, NotEquals, GreaterThan, LessThan);

}
