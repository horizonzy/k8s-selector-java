package com.horizonzy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {

    private Lexer lexer;

    private int position;

    private List<ScannedItem> scannedItems = new ArrayList<>();

    public Parser() {
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Lexer getLexer() {
        return lexer;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<ScannedItem> getScannedItems() {
        return scannedItems;
    }

    public void setScannedItems(List<ScannedItem> scannedItems) {
        this.scannedItems = scannedItems;
    }

    public Tuple<Integer, String> lookAhead(Integer context) {
        Integer token = scannedItems.get(position).getToken();
        String lit = scannedItems.get(position).getLiteral();

        if (ParseContext.Values == context) {
            if (Token.InToken == token || Token.NotInToken == token) {
                token = Token.IdentifierToken;
            }
        }
        return new Tuple<>(token, lit);
    }

    public void incPosition() {
        position++;
    }

    public Tuple<Integer, String> consume(Integer context) {
        position++;
        Integer token = scannedItems.get(position - 1).getToken();
        String lit = scannedItems.get(position - 1).getLiteral();
        if (ParseContext.Values == context) {
            if (Token.InToken == token || Token.NotInToken == token) {
                token = Token.IdentifierToken;
            }
        }
        return new Tuple<>(token, lit);
    }

    private void scan() {
        for (; ; ) {
            Tuple<Integer, String> tuple = lexer.lex();
            scannedItems.add(new ScannedItem(tuple.getKey(), tuple.getValue()));
            if (Token.EndOfStringToken == tuple.getKey()) {
                break;
            }
        }
    }

    public InternalSelector parse() {
        scan();
        InternalSelector requirements = new InternalSelector();
        for (; ; ) {
            Tuple<Integer, String> tuple = lookAhead(ParseContext.Values);
            Integer token = tuple.getKey();
            String lit = tuple.getValue();
            if (Token.IdentifierToken == token || Token.DoesNotExistToken == token) {
                Requirement requirement = parseRequirement();
                requirements.addRequire(requirement);
                Tuple<Integer, String> tuple2 = consume(ParseContext.Values);
                Integer t2 = tuple2.getKey();
                String l2 = tuple2.getValue();
                if (Token.EndOfStringToken == t2) {
                    return requirements;
                } else if (Token.CommaToken == t2) {
                    Tuple<Integer, String> tuple3 = lookAhead(ParseContext.Values);
                    Integer t3 = tuple3.getKey();
                    String l3 = tuple3.getValue();
                    if (Token.IdentifierToken != t3 && Token.DoesNotExistToken != t3) {
                        throw new IllegalArgumentException(
                                String.format("found '%s', expected: identifier after ','", l3));
                    }
                } else {
                    throw new IllegalArgumentException(
                            String.format("found '%s', expected: ',' or 'end of string'", l2));
                }
            } else if (Token.EndOfStringToken == token) {
                return requirements;
            } else {
                throw new IllegalArgumentException(
                        String.format("found '%s', expected : !, identifier, or 'end of string'",
                                lit));
            }
        }
    }

    public Requirement parseRequirement() {
        Tuple<String, String> tuple = parseKeyAndInferOperator();

        String key = tuple.getKey();
        String operator = tuple.getValue();

        if (Operator.Exists.equals(operator) || Operator.DoesNotExist.equals(operator)) {
            return new Requirement(key, operator, Collections.EMPTY_LIST);
        }

        operator = parseOperator();

        Set<String> values = null;
        if (Operator.In.equals(operator) || Operator.NotIn.equals(operator)) {
            values = parseValues();
        } else if (Operator.Equals.equals(operator) || Operator.DoubleEquals.equals(operator)
                || Operator.NotEquals.equals(operator) || Operator.GreaterThan.equals(operator)
                || Operator.LessThan.equals(operator)) {
            values = parseExactValue();
        }
        return Requirement.newRequirement(key, operator,
                values == null ? new ArrayList<>() : new ArrayList<>(values));

    }

    public Tuple<String, String> parseKeyAndInferOperator() {
        String operator = null;
        Integer token;
        String lit;
        Tuple<Integer, String> tuple1 = consume(ParseContext.Values);
        token = tuple1.getKey();
        lit = tuple1.getValue();
        if (Token.DoesNotExistToken == token) {
            operator = Operator.DoesNotExist;
            Tuple<Integer, String> tuple2 = consume(ParseContext.Values);
            token = tuple2.getKey();
            lit = tuple2.getValue();
        }
        if (Token.IdentifierToken != token) {
            throw new IllegalArgumentException(
                    String.format("found '%s', expected: identifier", lit));
        }

        Selector.validateLabelKey(lit);

        Integer t = lookAhead(ParseContext.Values).getKey();
        if (Token.EndOfStringToken == t || Token.CommaToken == t) {
            if (!Operator.DoesNotExist.equals(operator)) {
                operator = Operator.Exists;
            }
        }
        return new Tuple<>(lit, operator);
    }


    public String parseOperator() {
        Tuple<Integer, String> tuple = consume(ParseContext.KeyAndOperator);
        Integer token = tuple.getKey();
        String lit = tuple.getValue();

        switch (token) {
            case Token.InToken:
                return Operator.In;
            case Token.EqualsToken:
                return Operator.Equals;
            case Token.DoubleEqualsToken:
                return Operator.DoubleEquals;
            case Token.GreaterThanToken:
                return Operator.GreaterThan;
            case Token.LessThanToken:
                return Operator.LessThan;
            case Token.NotInToken:
                return Operator.NotIn;
            case Token.NotEqualsToken:
                return Operator.NotEquals;
            default:
                throw new IllegalArgumentException(String.format("found '%s', expected: %s", lit,
                        String.join(", ", Operator.unaryOperators)));
        }
    }

    private Set<String> parseValues() {
        Tuple<Integer, String> tuple = consume(ParseContext.Values);
        Integer token = tuple.getKey();
        String lit = tuple.getValue();

        if (Token.OpenParToken != token) {
            throw new IllegalArgumentException(String.format("found '%s' expected: '('", lit));
        }
        Tuple<Integer, String> tuple1 = lookAhead(ParseContext.Values);
        token = tuple1.getKey();
        lit = tuple1.getValue();

        if (Token.IdentifierToken == token || Token.CommaToken == token) {
            Set<String> s = parseIdentifiersList();
            Tuple<Integer, String> tuple2 = consume(ParseContext.Values);
            Integer token2 = tuple2.getKey();
            if (Token.ClosedParToken != token2) {
                throw new IllegalArgumentException(String.format("found '%s', expected: ')'", lit));
            }
            return s;
        } else if (Token.ClosedParToken == token) {
            incPosition();
            return new HashSet<>(Collections.singletonList(""));
        } else {
            throw new IllegalArgumentException(
                    String.format("found '%s', expected: ',', ')' or identifier", lit));
        }

    }

    private Set<String> parseIdentifiersList() {
        Set<String> result = new HashSet<>();

        for (; ; ) {
            Tuple<Integer, String> tuple = consume(ParseContext.Values);
            Integer token = tuple.getKey();
            String lit = tuple.getValue();

            if (Token.IdentifierToken == token) {
                result.add(lit);
                Tuple<Integer, String> tuple2 = lookAhead(ParseContext.Values);
                Integer token2 = tuple2.getKey();
                String lit2 = tuple2.getValue();
                if (Token.CommaToken == token2) {
                    continue;
                } else if (Token.ClosedParToken == token2) {
                    return result;
                } else {
                    throw new IllegalArgumentException(
                            String.format("found '%s', expected: ',' or ')'", lit2));
                }
            } else if (Token.CommaToken == token) {
                if (result.size() == 0) {
                    result.add("");
                }
                Tuple<Integer, String> tuple2 = lookAhead(ParseContext.Values);
                Integer token2 = tuple2.getKey();
                if (Token.ClosedParToken == token2) {
                    result.add("");
                    return result;
                }
                if (Token.CommaToken == token2) {
                    incPosition();
                    result.add("");
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("found '%s', expected: ',' or identifier", lit));
            }
        }
    }

    private Set<String> parseExactValue() {
        Set<String> result = new HashSet<>();

        Tuple<Integer, String> tuple = lookAhead(ParseContext.Values);
        Integer token = tuple.getKey();
        String lit = tuple.getValue();
        if (Token.EndOfStringToken == token || Token.CommaToken == token) {
            result.add("");
            return result;
        }
        incPosition();
        if (Token.IdentifierToken == token) {
            result.add(lit);
            return result;
        }
        throw new IllegalArgumentException(String.format("found '%s', expected: identifier", lit));
    }
}




