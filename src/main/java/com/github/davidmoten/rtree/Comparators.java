package com.github.davidmoten.rtree;

import java.util.Comparator;
import java.util.List;

import rx.functions.Func1;

import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.ListPair;
import com.github.davidmoten.rtree.geometry.Rectangle;

public final class Comparators {

    public static final Comparator<ListPair<?>> overlapListPairComparator = toComparator(Functions.overlapListPair);

    public static Comparator<ListPair<?>> areaPairComparator = new Comparator<ListPair<?>>() {

        @Override
        public int compare(ListPair<?> p1, ListPair<?> p2) {
            return ((Float) p1.areaSum()).compareTo(p2.areaSum());
        }
    };

    public static <T extends HasGeometry> Comparator<HasGeometry> overlapComparator(
            final Rectangle r, final List<T> list) {
        return toComparator(Functions.overlap(r, list));
    }

    public static <T extends HasGeometry> Comparator<HasGeometry> areaIncreaseComparator(
            final Rectangle r) {
        return toComparator(Functions.areaIncrease(r));
    }

    public static Comparator<HasGeometry> areaComparator(final Rectangle r) {
        return new Comparator<HasGeometry>() {

            @Override
            public int compare(HasGeometry g1, HasGeometry g2) {
                return ((Float) g1.geometry().mbr().add(r).area()).compareTo(g2.geometry().mbr()
                        .add(r).area());
            }
        };
    }

    public static <R, T extends Comparable<T>> Comparator<R> toComparator(final Func1<R, T> function) {
        return new Comparator<R>() {

            @Override
            public int compare(R g1, R g2) {
                return function.call(g1).compareTo(function.call(g2));
            }
        };
    }

    public static <T> Comparator<T> compose(final Comparator<T>... comparators) {
        return new Comparator<T>() {
            @Override
            public int compare(T t1, T t2) {
                for (Comparator<T> comparator : comparators) {
                    int value = comparator.compare(t1, t2);
                    if (value != 0)
                        return value;
                }
                return 0;
            }
        };
    }

}
