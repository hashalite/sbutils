package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//? if >=1.21.11 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//? }
//? yacl: >=3.6.0 {
import dev.isxander.yacl3.api.StateManager;
//? }
//? yacl: >=3.0.0 {
import dev.isxander.yacl3.api.controller.ControllerBuilder;
//? }

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

    //? yacl: <3.0.0
    //@SuppressWarnings("unchecked")
    protected static <T> Controller<T> dummyController(@Nullable String name,
                                                       Function<Option<T>,
                                                       //? yacl: >=3.0.0 {
                                                               ControllerBuilder<T>> controller,
                                                       //? } else
                                                               //Controller<T>> controller,
                                                       T def,
                                                       Supplier<T> get,
                                                       Consumer<T> set) {
        //? yacl: >=3.0.0 {
        return Option.<T>createBuilder()
        //? } else
        //return Option.<T>createBuilder((Class<T>)def.getClass())
                .name(name != null ? Component.translatable(name) : Component.literal(""))
                //? yacl: >=3.6.0 {
                .stateManager(StateManager.createInstant(def, get, set))
                //? } else {
                /*.binding(def, get, set)
                .instant(true)
                *///? }
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
        //? if >=1.21.11 {
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        //? } else
        //public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result ||
                        //? if >=1.21.11 {
                        element.mouseClicked(mouseButtonEvent, doubleClick);
                        //? } else
                        //element.mouseClicked(mouseX, mouseY, button);
            return result;
        }

        @Override
        //? if >=1.21.11 {
        public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        //? } else
        //public boolean mouseReleased(double mouseX, double mouseY, int button) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result ||
                        //? if >=1.21.11 {
                        element.mouseReleased(mouseButtonEvent);
                        //? } else
                        //element.mouseReleased(mouseX, mouseY, button);
            return result;
        }

        @Override
        //? if >=1.21.11 {
        public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        //? } else
        //public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result ||
                        //? if >=1.21.11 {
                        element.mouseDragged(mouseButtonEvent, dx, dy);
                        //? } else
                        //element.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            return result;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY,
                                     //? yacl: >=3.6.0 {
                                     double horizontalAmount, double verticalAmount
                                     //? } else
                                     //double delta
        ) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result || element.mouseScrolled(mouseX, mouseY,
                        //? yacl: >=3.6.0 {
                        horizontalAmount, verticalAmount
                        //? } else
                        //delta
                );
            return result;
        }

        @Override
        //? if >=1.21.11 {
        public boolean keyPressed(KeyEvent keyEvent) {
        //? } else
        //public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result ||
                        //? if >=1.21.11 {
                        element.keyPressed(keyEvent);
                        //? } else
                        //element.keyPressed(keyCode, scanCode, modifiers);
            return result;
        }

        @Override
        //? if >=1.21.11 {
        public boolean keyReleased(KeyEvent keyEvent) {
        //? } else
        //public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result ||
                        //? if >=1.21.11 {
                        element.keyReleased(keyEvent);
                        //? } else
                        //element.keyReleased(keyCode, scanCode, modifiers);
            return result;
        }

        @Override
        //? if >=1.21.11 {
        public boolean charTyped(CharacterEvent characterEvent) {
        //? } else
        //public boolean charTyped(char chr, int modifiers) {
            boolean result = false;
            for (AbstractWidget element : elements)
                result = result ||
                        //? if >=1.21.11 {
                        element.charTyped(characterEvent);
                        //? } else
                        //element.charTyped(chr, modifiers);
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
