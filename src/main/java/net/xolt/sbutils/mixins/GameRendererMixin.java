package net.xolt.sbutils.mixins;

import net.xolt.sbutils.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static net.xolt.sbutils.SbUtils.MC;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // When automining, ensure raycasting is done from the players perspective (in case player is using freecam)
    @ModifyVariable(method = "pick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity onUpdateTargetedEntity(Entity entity) {
        if (ModConfig.HANDLER.instance().autoMine.enabled && entity != MC.player) {
            return MC.player;
        }
        return entity;
    }
}
