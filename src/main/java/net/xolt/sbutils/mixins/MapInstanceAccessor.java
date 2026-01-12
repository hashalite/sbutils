package net.xolt.sbutils.mixins;

import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.21 {
import net.minecraft.client.resources.MapTextureManager;
//? } else {
/*import net.minecraft.client.gui.MapRenderer;
*///? }

//? if >=1.21 {
@Mixin(MapTextureManager.MapInstance.class)
//? } else
//@Mixin(MapRenderer.MapInstance.class)
public interface MapInstanceAccessor {

    @Accessor
    DynamicTexture getTexture();
}
