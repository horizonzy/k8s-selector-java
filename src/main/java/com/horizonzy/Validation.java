package com.horizonzy;

import java.util.regex.Pattern;

public class Validation {

    private static final int LabelValueMaxLength = 63;

    private static final int qualifiedNameMaxLength = 63;

    private static final int DNS1123SubdomainMaxLength = 253;

    private static final String dns1123LabelFmt = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";

    private static final String dns1123SubdomainFmt =
            dns1123LabelFmt + "(\\." + dns1123LabelFmt + ")*";

    private static final Pattern dns1123SubdomainRegexp = Pattern
            .compile("^" + dns1123SubdomainFmt + "$");

    private static final String dns1123SubdomainErrorMsg = "a lowercase RFC 1123 subdomain must consist of lower case alphanumeric characters, '-' or '.', and must start and end with an alphanumeric character";

    private static final String qnameCharFmt = "[A-Za-z0-9]";

    private static final String qnameExtCharFmt = "[-A-Za-z0-9_.]";

    private static final String qualifiedNameErrMsg = "must consist of alphanumeric characters, '-', '_' or '.', and must start and end with an alphanumeric character";

    private static final String labelValueErrMsg = "a valid label must be an empty string or consist of alphanumeric characters, '-', '_' or '.', and must start and end with an alphanumeric character";

    private static final String qualifiedNameFmt =
            "(" + qnameCharFmt + qnameExtCharFmt + "*)?" + qnameCharFmt;

    private static final Pattern qualifiedNameRegexp = Pattern
            .compile("^" + qualifiedNameFmt + "$");

    private static final String labelValueFmt = "(" + qualifiedNameFmt + ")?";

    private static final Pattern labelValueRegexp = Pattern.compile("^" + labelValueFmt + "$");


    public static void isQualifiedName(String value) {

        String[] parts = value.split("/");

        String name;
        if (parts.length == 1) {
            name = parts[0];
        } else if (parts.length == 2) {
            String prefix = parts[0];
            name = parts[1];
            if (prefix.length() == 0) {
                throw new IllegalArgumentException(nonEmptyError("prefix part"));
            } else {
                isDNS1123Subdomain(prefix);
            }
        } else {
            throw new IllegalArgumentException(
                    "a qualified name " + regexError(qualifiedNameErrMsg, qualifiedNameFmt,
                            "MyName", "my.name", "123-abc")
                            + " with an optional DNS subdomain prefix and '/' (e.g. 'example.com/MyName')");
        }

        if (name.length() == 0) {
            throw new IllegalArgumentException(nonEmptyError("name part"));
        } else if (name.length() > qualifiedNameMaxLength) {
            throw new IllegalArgumentException("name part " + maxLenError(qualifiedNameMaxLength));
        }
        if (!qualifiedNameRegexp.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "name part " + regexError(qualifiedNameErrMsg, qualifiedNameFmt, "MyName",
                            "my.name", "123-abc"));
        }

    }

    public static void isDNS1123Subdomain(String value) {
        if (value.length() > DNS1123SubdomainMaxLength) {
            throw new IllegalArgumentException(maxLenError(DNS1123SubdomainMaxLength));
        }
        if (dns1123SubdomainRegexp.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    regexError(dns1123SubdomainErrorMsg, dns1123SubdomainFmt, "example.com"));
        }
    }

    public static void isValidLabelValue(String value) {
        if (value.length() > LabelValueMaxLength) {
            throw new IllegalArgumentException(maxLenError(LabelValueMaxLength));
        }
        if (!labelValueRegexp.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    regexError(labelValueErrMsg, labelValueFmt, "MyValue", "my_value", "12345"));
        }

    }

    private static String maxLenError(int length) {
        return String.format("must be no more than %d characters", length);
    }

    private static String nonEmptyError(String param) {
        return param + " must be non-empty";
    }

    private static String regexError(String msg, String fmt, String... examples) {
        if (examples.length == 0) {
            return msg + " (regex used for validation is '" + fmt + "')";
        }
        msg += " (e.g. ";

        int index = 0;
        StringBuilder msgBuilder = new StringBuilder(msg);
        for (String example : examples) {
            if (index > 0) {
                msgBuilder.append(" or ");
            }
            index++;
            msgBuilder.append("'").append(example).append("', ");
        }
        msg = msgBuilder.toString();
        msg += "regex used for validation is '" + fmt + "')";
        return msg;
    }

}
