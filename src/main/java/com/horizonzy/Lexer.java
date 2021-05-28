package com.horizonzy;


public class Lexer {

    private String s;

    private int pos;

    public Lexer(String s) {
        this(s, 0);
    }

    public Lexer(String s, int pos) {
        this.s = s;
        this.pos = pos;
    }

    public byte read() {
        byte b = 0;
        if (this.pos < s.length()) {
            b = (byte) s.charAt(pos);
            pos++;
        }
        return b;
    }

    public void unread() {
        pos--;
    }

    public Tuple<Integer, String> scanIDOrKeyword() {
        byte[] buffer = new byte[s.length()];
        int index = 0;
        for (; ; ) {
            byte ch = read();
            if (ch == 0) {
                break;
            } else if (Selector.isSpecialSymbol(ch) || Selector.isWhiteSpace(ch)) {
                unread();
                break;
            } else {
                buffer[index++] = ch;
            }
        }
        String s = new String(buffer, 0, index);

        Integer token = Selector.string2Token.get(s);
        if (token == null) {
            return new Tuple<>(Token.IdentifierToken, s);
        }
        return new Tuple<>(token, s);
    }

    public Tuple<Integer, String> scanSpecialSymbol() {
        ScannedItem lastScannedItem = new ScannedItem();
        byte[] buffer = new byte[s.length()];
        int index = 0;

        for (; ; ) {
            byte ch = read();
            if (ch == 0) {
                break;
            } else if (Selector.isSpecialSymbol(ch)) {
                buffer[index++] = ch;
                String symbol = new String(buffer, 0, index);
                Integer token = Selector.string2Token.get(symbol);
                if (token != null) {
                    lastScannedItem = new ScannedItem(token, symbol);
                } else if (lastScannedItem.getToken() != null) {
                    unread();
                    break;
                }
            } else {
                unread();
                break;
            }
        }
        if (lastScannedItem.getToken() == null) {
            return new Tuple<>(Token.ErrorToken,
                    "error expected: keyword found" + new String(buffer, 0, index).intern());
        }
        return new Tuple<>(lastScannedItem.getToken(), lastScannedItem.getLiteral());
    }

    public byte skipWhiteSpaces(byte ch) {
        for (; ; ) {
            if (!Selector.isWhiteSpace(ch)) {
                return ch;
            }
            ch = read();
        }
    }

    public Tuple<Integer, String> lex() {
        byte ch = skipWhiteSpaces(read());
        if (ch == 0) {
            return new Tuple<>(Token.EndOfStringToken, "");
        } else if (Selector.isSpecialSymbol(ch)) {
            unread();
            return scanSpecialSymbol();
        } else {
            unread();
            return scanIDOrKeyword();
        }
    }

}
