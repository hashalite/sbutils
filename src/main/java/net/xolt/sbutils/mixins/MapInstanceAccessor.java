package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapRenderer.MapInstance.class)
public interface MapInstanceAccessor {

    @Accessor
    DynamicTexture getTexture();
}
