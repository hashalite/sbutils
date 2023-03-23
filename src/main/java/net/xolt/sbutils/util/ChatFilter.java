package net.xolt.sbutils.util;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

public class ChatFilter {

    private final List<Pattern> regex;
    private final String key;
    private final BooleanSupplier enabled;

    public ChatFilter(String key, List<Pattern> regex, BooleanSupplier enabled) {
        this.regex = regex;
        this.key = key;
        this.enabled = enabled;
    }

    public String getKey() {
        return key;
    }

    public boolean matches(String string) {
        for (Pattern pattern : regex) {
            if (pattern.matcher(string).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }
}
