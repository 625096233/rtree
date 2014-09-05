package com.github.davidmoten.rtree;

import static com.google.common.base.Optional.of;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.common.base.Optional;

public final class SelectorMinimalAreaIncrease implements Selector {

	@Override
	public <T> Node<T> select(Geometry g, List<? extends Node<T>> nodes) {
		return findLeastIncreaseInMbrArea(g.mbr(), nodes);
	}

	static <T> Node<T> findLeastIncreaseInMbrArea(Rectangle r,
			List<? extends Node<T>> list) {
		List<Node<T>> best = new ArrayList<Node<T>>();
		Optional<Double> bestMetric = Optional.absent();
		for (Node<T> node : list) {
			double m = node.geometry().mbr().add(r).area()
					- node.geometry().mbr().area();
			if (!bestMetric.isPresent() || m < bestMetric.get()) {
				bestMetric = of(m);
				best = new ArrayList<Node<T>>();
				best.add(node);
			} else if (bestMetric.isPresent() && m == bestMetric.get()) {
				best.add(node);
			}
		}
		// TODO optimise, only need first
		TreeSet<Node<T>> ordered = new TreeSet<Node<T>>(increaseInArea(r));
		ordered.addAll(best);
		return ordered.first();
	}

	private static Comparator<Node<?>> increaseInArea(final Rectangle r) {
		return new Comparator<Node<?>>() {
			@Override
			public int compare(Node<?> n1, Node<?> n2) {
				return ((Float) n1.geometry().mbr().add(r).area()).compareTo(n2
						.geometry().mbr().add(r).area());
			}
		};
	}

}
