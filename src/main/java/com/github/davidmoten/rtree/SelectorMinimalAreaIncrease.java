package com.github.davidmoten.rtree;

import static com.google.common.base.Optional.of;

import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.common.base.Optional;

public class SelectorMinimalAreaIncrease implements Selector {

    @Override
    public <T> Node<T> select(Geometry g, List<? extends Node<T>> nodes) {
        return findLeastIncreaseInMbrArea(g.mbr(), nodes);
    }

    private static <T> Node<T> findLeastIncreaseInMbrArea(Rectangle r, List<? extends Node<T>> list) {
        Optional<Double> minDifference = Optional.absent();
        Optional<Node<T>> minDiffItem = Optional.absent();
        for (Node<T> m : list) {
            double diff = m.geometry().mbr().add(r).area() - m.geometry().mbr().area();
            if (!minDifference.isPresent() || diff < minDifference.get()) {
                minDifference = of(diff);
                minDiffItem = of(m);
            }
        }
        return minDiffItem.get();
    }

}
