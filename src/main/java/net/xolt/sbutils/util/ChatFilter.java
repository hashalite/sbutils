package net.xolt.sbutils.util;

import dev.isxander.yacl.config.ConfigInstance;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.binding.OptionBinding;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter<C> {

    private final List<Pattern> regex;
    private final OptionBinding<C, Boolean> option;
    private final ConfigInstance<C> configHandler;
    private final boolean titles;

    public ChatFilter(OptionBinding<C, Boolean> option, ConfigInstance<C> configHandler, List<Pattern> regex) {
        this(option, configHandler, regex, false);
    }

    public ChatFilter(OptionBinding<C, Boolean> option, ConfigInstance<C> configHandler, List<Pattern> regex, boolean titles) {
        this.regex = regex;
        this.option = option;
        this.configHandler = configHandler;
        this.titles = titles;
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

    public boolean titles() {
        return titles;
    }
}
