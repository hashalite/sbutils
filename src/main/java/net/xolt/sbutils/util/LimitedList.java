package net.xolt.sbutils.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LimitedList<E> extends ArrayList<E> {

    private final int maxSize;

    @SafeVarargs
    public LimitedList(int maxSize, E ... args) {
        this(maxSize, Arrays.asList(args));
    }

    public LimitedList(int maxSize, List<E> elements) {
        super();
        this.maxSize = maxSize;
        addAll(elements);
    }

    @Override
    public boolean add(E e) {
        if (this.size() >= maxSize) {
            return false;
        }
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        if (this.size() >= maxSize) {
            return;
        }
        super.add(index, element);
    }

    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c.stream().limit(maxSize - this.size()).toList());
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return super.addAll(index, c.stream().limit(maxSize - this.size()).toList());
    }
}
