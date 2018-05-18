package cappuccino.decompiler.parser;

import org.antlr.v4.runtime.Token;

public class Utils {

    public static String stripContainingQuotes(Token token) {
        return stripContainingQuotes(token.getText());
    }

    public static String stripContainingQuotes(String string) {
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length()-1);
        }
        return string;
    }
}
