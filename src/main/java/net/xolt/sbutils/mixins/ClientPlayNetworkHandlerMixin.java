package net.xolt.sbutils.mixins;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.features.*;
import net.xolt.sbutils.features.common.ServerDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private CombinedDynamicRegistries<ClientDynamicRegistryType> combinedDynamicRegistries;

    @Shadow
    private FeatureSet enabledFeatures;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        ServerDetector.onJoinGame();
        AutoAdvert.onJoinGame();
        JoinCommands.onJoinGame();
        AutoRaffle.onJoinGame();
    }

    @Inject(method = "onPlayerRemove", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci, Iterator var2, UUID uUID, PlayerListEntry playerListEntry) {
        StaffDetector.onPlayerLeave(playerListEntry);
    }

    @Inject(method = "onPlayerRemove", at = @At("TAIL"))
    private void onPlayerRemoveTail(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
        StaffDetector.afterPlayerLeave();
    }

    @Inject(method = "handlePlayerListAction", at = @At("HEAD"))
    private void onHandlePlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry, CallbackInfo ci) {
        if (action != PlayerListS2CPacket.Action.UPDATE_LISTED) {
            return;
        }

        if (receivedEntry.listed()) {
            StaffDetector.onPlayerJoin(currentEntry);
        } else {
            StaffDetector.onPlayerLeave(currentEntry);
        }
    }

    @Inject(method = "handlePlayerListAction", at = @At("TAIL"))
    private void onHandlePlayerListActionTail(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry, CallbackInfo ci) {
        if (action != PlayerListS2CPacket.Action.UPDATE_LISTED) {
            return;
        }

        if (!receivedEntry.listed()) {
            StaffDetector.afterPlayerLeave();
        }
    }

    @Inject(method = "onSignEditorOpen", at = @At("HEAD"), cancellable = true)
    private void onSignEditorOpen(SignEditorOpenS2CPacket packet, CallbackInfo ci) {
        if (AutoPrivate.onSignEditorOpen(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScreenHandlerPropertyUpdate", at = @At("HEAD"))
    private void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet, CallbackInfo ci) {
        if (MC.currentScreen instanceof EnchantmentScreen && ((EnchantmentScreen) MC.currentScreen).getScreenHandler().syncId == packet.getSyncId()) {
            AutoSilk.onEnchantUpdate();
        }
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        AutoFix.onUpdateInventory();
        AutoSilk.onInventoryUpdate(packet);
    }

    // Remove conflicting server commands from command tree
    // Prevents server command suggestions from interfering with sbutils command suggestions
    @ModifyVariable(method = "onCommandTree", at = @At("HEAD"), argsOnly = true)
    private CommandTreeS2CPacket onCommandTree(CommandTreeS2CPacket packet) {
        RootCommandNode<CommandSource> rootNode = packet.getCommandTree(CommandRegistryAccess.of((RegistryWrapper.WrapperLookup)this.combinedDynamicRegistries.getCombinedRegistryManager(), this.enabledFeatures));
        Collection<CommandNode<CommandSource>> nodes = rootNode.getChildren();

        nodes.removeIf((node) -> SbUtils.commands.contains(node.getName()));

        RootCommandNode<CommandSource> newRootNode = new RootCommandNode<>();

        nodes.forEach(newRootNode::addChild);

        return new CommandTreeS2CPacket(newRootNode);
    }

    @Inject(method = "sendPacket", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClickSlotC2SPacket) {
            AutoFix.onUpdateInventory();
        }
    }

    @ModifyVariable(method = "sendPacket", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        if (packet instanceof ChatMessageC2SPacket) {
            return ChatAppend.processSentMessage((ChatMessageC2SPacket)packet);
        }
        return packet;
    }
}
