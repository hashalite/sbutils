package net.xolt.sbutils.mixins;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.xolt.sbutils.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // When automining, ensure raycasting is done from the players perspective (in case player is using freecam)
    @ModifyVariable(method = "updateTargetedEntity", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity onUpdateTargetedEntity(Entity entity) {
        if (ModConfig.INSTANCE.getConfig().autoMine && entity != MC.player) {
            return MC.player;
        }
        return entity;
    }
}
