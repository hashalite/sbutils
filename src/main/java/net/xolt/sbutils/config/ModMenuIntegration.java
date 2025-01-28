package net.xolt.sbutils.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.gui.ConfigGuiFactory;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SbUtils.GUI_FACTORY::getConfigScreen;
    }
}
