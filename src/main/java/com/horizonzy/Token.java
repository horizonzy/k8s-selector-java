package com.horizonzy;

public class Token {

    // ErrorToken represents scan error
    public static final int ErrorToken = 0;

    // EndOfStringToken represents end of string
    public static final int EndOfStringToken = 1;

    // ClosedParToken represents close parenthesis
    public static final int ClosedParToken = 2;

    // CommaToken represents the comma
    public static final int CommaToken = 3;

    // DoesNotExistToken represents logic not
    public static final int DoesNotExistToken = 4;

    // DoubleEqualsToken represents double equals
    public static final int DoubleEqualsToken = 5;

    // EqualsToken represents equal
    public static final int EqualsToken = 6;

    // GreaterThanToken represents greater than
    public static final int GreaterThanToken = 7;

    // IdentifierToken represents identifier, e.g. keys and values
    public static final int IdentifierToken = 8;

    // InToken represents in
    public static final int InToken = 9;

    // LessThanToken represents less than
    public static final int LessThanToken = 10;

    // NotEqualsToken represents not equal
    public static final int NotEqualsToken = 11;

    // NotInToken represents not in
    public static final int NotInToken = 12;

    // OpenParToken represents open parenthesis
    public static final int OpenParToken = 13;


}
