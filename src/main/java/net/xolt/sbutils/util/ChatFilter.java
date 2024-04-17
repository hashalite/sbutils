package net.xolt.sbutils.util;

import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.OptionBinding;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter {

    private final List<Pattern> regex;
    private final OptionBinding<Boolean> option;

    public ChatFilter(OptionBinding<Boolean> option, List<Pattern> regex) {
        this.regex = regex;
        this.option = option;
    }

    public MutableComponent getName() {
        return option.getName();
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
        return option.get(ModConfig.HANDLER.instance());
    }

    public MutableComponent format() {
        return TextUtils.insertPlaceholders("message.sbutils.chatFilter.statusFormat", getName(), isEnabled());
    }
}
