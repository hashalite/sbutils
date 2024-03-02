package net.xolt.sbutils.config.binding.constraints;

import net.xolt.sbutils.config.binding.Constraints;
import org.jetbrains.annotations.Nullable;

public class StringConstraints implements Constraints<String> {
    private final Boolean spacesAllowed;

    public StringConstraints(@Nullable Boolean spacesAllowed) {
        this.spacesAllowed = spacesAllowed == null || spacesAllowed;
    }

    @Override
    public boolean isValid(String input) {
        return !(!getSpacesAllowed() && input.contains(" "));
    }

    @Override
    public String validate(String input) {
        if (isValid(input))
            return input;
        return input.substring(0, input.indexOf(' '));
    }

    public Boolean getSpacesAllowed() {
        return spacesAllowed;
    }
}
