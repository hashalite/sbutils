package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class MultiValueController<T> implements Controller<T> {
    private final Option<T> option;
    private final List<Integer> ratios;
    private final List<Controller<?>> controllers;

    public MultiValueController(Option<T> option, List<Integer> ratios, List<Controller<?>> controllers) {
        if (ratios.size() != controllers.size())
            throw new IllegalArgumentException("Number of provided value names and controllers is not equal");
        this.option = option;
        this.ratios = ratios;
        this.controllers = controllers;
    }

    protected static <T> Controller<T> dummyController(@Nullable String name, Function<Option<T>, ControllerBuilder<T>> controller, T def, Supplier<T> get, Consumer<T> set) {
        return Option.<T>createBuilder()
                .name(name != null ? Component.translatable(name) : Component.literal(""))
                .binding(
                        def,
                        get,
                        set
                )
                .instant(true)
                .controller(controller).build().controller();
    }

    @Override
    public Option<T> option() {
        return option;
    }

    @Override
    public Component formatValue() {
        List<Component> formatted = controllers.stream().map(Controller::formatValue).toList();
        MutableComponent result = formatted.get(0).copy();
        for (int i = 1; i < formatted.size(); i++) {
            result.append(" | ").append(formatted.get(i));
        }
        return result;
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        List<AbstractWidget> elements = new ArrayList<>();
        List<Dimension<Integer>> dimensions = MultiValueElement.calcDimensions(widgetDimension, ratios);
        for (int i = 0; i < controllers.size(); i++) {
            elements.add(controllers.get(i).provideWidget(screen, dimensions.get(i)));
        }
        return new MultiValueElement(this, screen, widgetDimension, elements, ratios);
    }

    public static class MultiValueElement extends ControllerWidget<MultiValueController<?>> {
        private final List<AbstractWidget> elements;
        private final List<Integer> ratios;

        public MultiValueElement(MultiValueController control, YACLScreen screen, Dimension<Integer> dim, List<AbstractWidget> elements, List<Integer> ratios) {
            super(control, screen, dim);
            this.elements = elements;
            this.ratios = ratios;
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            elements.forEach((element) -> element.mouseMoved(mouseX, mouseY));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.mouseClicked(mouseX, mouseY, button);
            return result;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.mouseReleased(mouseX, mouseY, button);
            return result;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            return result;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return result;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.keyPressed(keyCode, scanCode, modifiers);
            return result;
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.keyReleased(keyCode, scanCode, modifiers);
            return result;
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.charTyped(chr, modifiers);
            return result;
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        public void setDimension(Dimension<Integer> dim) {
            List<Dimension<Integer>> dimensions = calcDimensions(dim, ratios);
            for (int i = 0; i < elements.size(); i++)
                elements.get(i).setDimension(dimensions.get(i));
            super.setDimension(dim);
        }

        public static List<Dimension<Integer>> calcDimensions(Dimension<Integer> parentDim, List<Integer> ratios) {
            List<Dimension<Integer>> result = new ArrayList<>();
            int ratioTotal = ratios.stream().mapToInt(Integer::intValue).sum();
            int offset = 0;
            for (Integer ratio : ratios) {
                double widthPercent = (double) ratio / ratioTotal;
                Dimension<Integer> dimension = parentDim.moved(offset, 0).withWidth((int) (widthPercent * parentDim.width()));
                offset += dimension.width();
                result.add(dimension);

            }
            return result;
        }

        @Override
        public void unfocus() {
            elements.forEach(AbstractWidget::unfocus);
            super.unfocus();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            elements.forEach((element) -> element.render(graphics, mouseX, mouseY, delta));
        }
    }
}
