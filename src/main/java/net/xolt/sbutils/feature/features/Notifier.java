package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ApiUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.ArrayList;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class Notifier extends Feature {
    private final OptionBinding<Boolean> showLlamaTitle = new OptionBinding<>("notifier.showLlamaTitle", Boolean.class, (config) -> config.notifier.showLlamaTitle, (config, value) -> config.notifier.showLlamaTitle = value);
    private final OptionBinding<Boolean> playLlamaSound = new OptionBinding<>("notifier.playLlamaSound", Boolean.class, (config) -> config.notifier.playLlamaSound, (config, value) -> config.notifier.playLlamaSound = value);
    private final OptionBinding<ModConfig.NotifSound> llamaSound = new OptionBinding<>("notifier.llamaSound", ModConfig.NotifSound.class, (config) -> config.notifier.llamaSound, (config, value) -> config.notifier.llamaSound = value);
    private final OptionBinding<Boolean> showTraderTitle = new OptionBinding<>("notifier.showTraderTitle", Boolean.class, (config) -> config.notifier.showTraderTitle, (config, value) -> config.notifier.showTraderTitle = value);
    private final OptionBinding<Boolean> showTraderItems = new OptionBinding<>("notifier.showTraderItems", Boolean.class, (config) -> config.notifier.showTraderItems, (config, value) -> config.notifier.showTraderItems = value);
    private final OptionBinding<Boolean> showTradesOnClick = new OptionBinding<>("notifier.showTradesOnClick", Boolean.class, (config) -> config.notifier.showTradesOnClick, (config, value) -> config.notifier.showTradesOnClick = value);
    private final OptionBinding<Boolean> playTraderSound = new OptionBinding<>("notifier.playTraderSound", Boolean.class, (config) -> config.notifier.playTraderSound, (config, value) -> config.notifier.playTraderSound = value);
    private final OptionBinding<ModConfig.NotifSound> traderSound = new OptionBinding<>("notifier.traderSound", ModConfig.NotifSound.class, (config) -> config.notifier.traderSound, (config, value) -> config.notifier.traderSound = value);
    private final OptionBinding<Boolean> playShopSound = new OptionBinding<>("notifier.playShopSound", Boolean.class, (config) -> config.notifier.playShopSound, (config, value) -> config.notifier.playShopSound = value);
    private final OptionBinding<ModConfig.NotifSound> shopSound = new OptionBinding<>("notifier.shopSound", ModConfig.NotifSound.class, (config) -> config.notifier.shopSound, (config, value) -> config.notifier.shopSound = value);
    private final OptionBinding<Boolean> playVisitSound = new OptionBinding<>("notifier.playVisitSound", Boolean.class, (config) -> config.notifier.playVisitSound, (config, value) -> config.notifier.playVisitSound = value);
    private final OptionBinding<ModConfig.NotifSound> visitSound = new OptionBinding<>("notifier.visitSound", ModConfig.NotifSound.class, (config) -> config.notifier.visitSound, (config, value) -> config.notifier.visitSound = value);


    private List<ItemStack> traderItems;
    private int showTraderItemsTicks;

    public Notifier() {
        super("notifier", "notifier", "notif");
        showTraderItemsTicks = 0;
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(showLlamaTitle, playLlamaSound, llamaSound, showTraderTitle, showTraderItems, showTradesOnClick, playTraderSound, traderSound, playShopSound, shopSound, playVisitSound, visitSound);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> eventNotifierNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(ClientCommandManager.literal("vpLlama")
                        .then(CommandHelper.bool("title", showLlamaTitle))
                        .then(CommandHelper.bool("playSound", playLlamaSound))
                        .then(CommandHelper.genericEnum("sound", "sound", llamaSound)))
                .then(ClientCommandManager.literal("trader")
                        .then(CommandHelper.bool("title", showTraderTitle))
                        .then(CommandHelper.bool("items", showTraderItems)
                                .then(CommandHelper.bool("onClick", showTradesOnClick)))
                        .then(CommandHelper.runnable("checkItems", this::displayTraderItems))
                        .then(CommandHelper.bool("playSound", playTraderSound))
                        .then(CommandHelper.genericEnum("sound", "sound", traderSound)))
                .then(CommandHelper.bool("shop", playShopSound)
                        .then(CommandHelper.genericEnum("sound", "sound", shopSound)))
                .then(CommandHelper.bool("visits", playVisitSound)
                        .then(CommandHelper.genericEnum("sound", "sound", visitSound)))
        );
        registerAlias(dispatcher, eventNotifierNode);
    }

    public void tick() {
        if (showTraderItemsTicks > 0)
            showTraderItemsTicks--;
    }

    public void onRenderGui(GuiGraphics guiGraphics) {
        if (traderItems == null || showTraderItemsTicks <= 0 || MC.getConnection() == null)
            return;

        guiGraphics.drawCenteredString(MC.font, Component.translatable("message.sbutils.notifier.traderItems"), guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() - 88, ModConfig.HANDLER.instance().messageColor.getRGB());
        int itemsWidth = traderItems.size() * 16;
        for (int i = 0; i < traderItems.size(); i++) {
            int x = (i * 16) + ((guiGraphics.guiWidth() - itemsWidth) / 2);
            guiGraphics.renderItem(traderItems.get(i), x, guiGraphics.guiHeight() - 75);
        }
        guiGraphics.renderOutline(((guiGraphics.guiWidth() - itemsWidth) / 2) - 2, guiGraphics.guiHeight() - 77, itemsWidth + 4, 20, ModConfig.HANDLER.instance().messageColor.getRGB());
    }

    public void processMessage(Component message) {
        if (!enabled())
            return;

        String stringMessage = message.getString();

        if (RegexFilters.vpLlamaFilter.matcher(stringMessage).matches())
            doLlamaNotification();
        else if (RegexFilters.wanderingTraderFilter.matcher(stringMessage).matches()) {
            if (ModConfig.HANDLER.instance().notifier.showTraderItems)
                displayTraderItems();
            doTraderNotification();
        } else if (RegexFilters.incomingTransactionFilter.matcher(stringMessage).matches()) {
            doShopNotification();
        } else if (RegexFilters.visitFilter.matcher(stringMessage).matches()) {
            doVisitNotification();
        }
    }

    public static Component modifyMessage(Component message) {
        MutableComponent result = message.copy().withStyle(message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/notifier trader checkItems")));
        List<Component> siblings = new ArrayList<>(result.getSiblings());
        result.getSiblings().clear();
        for (Component sibling : siblings)
            result.append(modifyMessage(sibling));
        return result;
    }



    public static boolean shouldModify(Component message) {
        return ModConfig.HANDLER.instance().notifier.showTradesOnClick && RegexFilters.wanderingTraderFilter.matcher(message.getString()).matches();
    }

    private void displayTraderItems() {
        ApiUtils.getWanderingTrades(SbUtils.SERVER_DETECTOR.getCurrentServer(), this::onReceiveTraderItems);
    }

    private void onReceiveTraderItems(List<ItemStack> items) {
        traderItems = items;
        showTraderItemsTicks = 100;
    }

    private static void doLlamaNotification() {
        if (ModConfig.HANDLER.instance().notifier.playLlamaSound && MC.player != null)
            MC.player.playSound(ModConfig.HANDLER.instance().notifier.llamaSound.getSound(), 1, 1);

        if (ModConfig.HANDLER.instance().notifier.showLlamaTitle)
            ChatUtils.sendPlaceholderTitle("message.sbutils.notifier.sighted", Component.translatable("message.sbutils.notifier.vpLlama"));
    }

    private static void doTraderNotification() {
        if (ModConfig.HANDLER.instance().notifier.playTraderSound && MC.player != null)
            MC.player.playSound(ModConfig.HANDLER.instance().notifier.traderSound.getSound(), 1, 1);

        if (ModConfig.HANDLER.instance().notifier.showTraderTitle)
            ChatUtils.sendPlaceholderTitle("message.sbutils.notifier.sighted", Component.translatable("message.sbutils.notifier.wanderingTrader"));
    }

    private static void doShopNotification() {
        if (ModConfig.HANDLER.instance().notifier.playShopSound && MC.player != null)
            MC.player.playSound(ModConfig.HANDLER.instance().notifier.shopSound.getSound(), 1, 1);
    }

    private static void doVisitNotification() {
        if (ModConfig.HANDLER.instance().notifier.playVisitSound && MC.player != null)
            MC.player.playSound(ModConfig.HANDLER.instance().notifier.visitSound.getSound(), 1, 1);
    }

    private static boolean enabled() {
        return ModConfig.HANDLER.instance().notifier.showLlamaTitle ||
                ModConfig.HANDLER.instance().notifier.playLlamaSound ||
                ModConfig.HANDLER.instance().notifier.showTraderTitle ||
                ModConfig.HANDLER.instance().notifier.playTraderSound ||
                ModConfig.HANDLER.instance().notifier.playShopSound ||
                ModConfig.HANDLER.instance().notifier.playVisitSound;
    }
}
