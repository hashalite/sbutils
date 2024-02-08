package net.xolt.sbutils.config;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;

import java.util.function.Function;

public class KeyValueControllerBuilder<K, V> implements ControllerBuilder<KeyValueController.KeyValuePair<K, V>> {
    protected final Option<KeyValueController.KeyValuePair<K, V>> option;
    private String keyName;
    private Function<Option<K>, ControllerBuilder<K>> keyController;
    private String valueName;
    private Function<Option<V>, ControllerBuilder<V>> valueController;
    private Double ratio;

    public KeyValueControllerBuilder(Option<KeyValueController.KeyValuePair<K, V>> option) {
        this.option = option;
    }

    static <C, D> KeyValueControllerBuilder<C, D> create(Option<KeyValueController.KeyValuePair<C, D>> option) {
        return new KeyValueControllerBuilder<>(option);
    }

    public KeyValueControllerBuilder<K, V> keyController(String keyName, Function<Option<K>, ControllerBuilder<K>> keyController) {
        this.keyName = keyName;
        this.keyController = keyController;
        return this;
    }

    public KeyValueControllerBuilder<K, V> valueController(String valueName, Function<Option<V>, ControllerBuilder<V>> valueController) {
        this.valueName = valueName;
        this.valueController = valueController;
        return this;
    }

    public KeyValueControllerBuilder<K, V> ratio(double ratio) {
        this.ratio = ratio;
        return this;
    }

    @Override public Controller<KeyValueController.KeyValuePair<K, V>> build() {
        return new KeyValueController<K, V>(option, ratio, keyName, keyController, valueName, valueController);
    }
}
