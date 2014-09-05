package com.github.davidmoten.rtree;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.common.base.Optional;

public class SelectorMinimalOverlap implements Selector {

	@Override
	public <T> Node<T> select(Geometry g, List<? extends Node<T>> nodes) {
		return findMinimalOverlap(g.mbr(), nodes);
	}

	private static <T> Node<T> findMinimalOverlap(Rectangle r,
			List<? extends Node<T>> list) {
		List<Node<T>> best = new ArrayList<Node<T>>();
		Optional<Double> bestMetric = Optional.absent();
		for (Node<T> node : list) {
			double m = 0;
			for (Node<T> node2 : list) {
				Rectangle nodePlusR = node.geometry().mbr().add(r);
				if (node2 != node) {
					m += nodePlusR.intersectionArea(node2.geometry().mbr());
				}
			}
			if (!bestMetric.isPresent() || m < bestMetric.get()) {
				best = new ArrayList<Node<T>>();
				best.add(node);
				bestMetric = Optional.of(m);
			} else if (bestMetric.isPresent() && m == bestMetric.get()) {
				best.add(node);
			}
		}
		return SelectorMinimalAreaIncrease.findMinimalAreaIncrease(r, best);
	}
}
