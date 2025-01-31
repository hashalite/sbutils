package net.xolt.sbutils.util;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.OptionBinding;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter<C> {

    private final List<Pattern> regex;
    private final OptionBinding<C, Boolean> option;
    private final ConfigClassHandler<C> configHandler;

    public ChatFilter(OptionBinding<C, Boolean> option, ConfigClassHandler<C> configHandler, List<Pattern> regex) {
        this.regex = regex;
        this.option = option;
        this.configHandler = configHandler;
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
        return option.get(configHandler.instance());
    }

    public MutableComponent format() {
        return TextUtils.insertPlaceholders("message.sbutils.chatFilter.statusFormat", getName(), isEnabled());
    }
}
