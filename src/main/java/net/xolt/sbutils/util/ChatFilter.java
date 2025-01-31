package net.xolt.sbutils.util;

import dev.isxander.yacl3.config.ConfigInstance;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.binding.OptionBinding;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter<C> {

    private final List<Pattern> regex;
    private final OptionBinding<C, Boolean> option;
    private final ConfigInstance<C> configHandler;

    public ChatFilter(OptionBinding<C, Boolean> option, ConfigInstance<C> configHandler, List<Pattern> regex) {
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
        return option.get(configHandler.getConfig());
    }

    public MutableComponent format() {
        return TextUtils.insertPlaceholders("message.sbutils.chatFilter.statusFormat", getName(), isEnabled());
    }
}
