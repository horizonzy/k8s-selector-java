package com.horizonzy;


import com.sun.xml.internal.fastinfoset.algorithm.IEEE754FloatingPointEncodingAlgorithm;
import java.util.HashMap;
import java.util.Map;

public class Selector {


    public static Map<String, Integer> string2Token = new HashMap<>();

    static {
        string2Token.put(")", Token.ClosedParToken);
        string2Token.put(",", Token.CommaToken);
        string2Token.put("!", Token.DoesNotExistToken);
        string2Token.put("==", Token.DoubleEqualsToken);
        string2Token.put("=", Token.EqualsToken);
        string2Token.put(">", Token.GreaterThanToken);
        string2Token.put("in", Token.InToken);
        string2Token.put("<", Token.LessThanToken);
        string2Token.put("!=", Token.NotEqualsToken);
        string2Token.put("notin", Token.NotInToken);
        string2Token.put("(", Token.OpenParToken);
    }

    public static InternalSelector parse(String selector) {
        Lexer lexer = new Lexer(selector, 0);
        Parser parser = new Parser(lexer);
        InternalSelector items = parser.parse();
        items.sort();
        return items;
    }


    public static boolean isSpecialSymbol(byte ch) {
        if (ch == '=') {
            return true;
        }
        if (ch == '!') {
            return true;
        }
        if (ch == '(') {
            return true;
        }
        if (ch == ')') {
            return true;
        }
        if (ch == ',') {
            return true;
        }
        if (ch == '>') {
            return true;
        }
        return ch == '<';
    }

    public static boolean isWhiteSpace(byte ch) {
        if (ch == ' ') {
            return true;
        }
        if (ch == '\t') {
            return true;
        }
        if (ch == '\r') {
            return true;
        }
        return ch == '\n';
    }

    public static void validateLabelKey(String key) {
        Validation.isQualifiedName(key);
    }

    public static void validateLabelValue(String key, String value) {
        Validation.isValidLabelValue(value);
    }

}

