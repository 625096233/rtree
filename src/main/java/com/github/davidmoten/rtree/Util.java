package com.github.davidmoten.rtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class Util {

	public static Rectangle mbr(Collection<? extends HasMbr> items) {
		Preconditions.checkArgument(!items.isEmpty());
		Optional<Rectangle> r = Optional.absent();
		for (HasMbr mbr : items) {
			if (r.isPresent())
				r = Optional.of(r.get().add(mbr.mbr()));
			else
				r = Optional.of(mbr.mbr());
		}
		return r.get();
	}

	public static <T> List<T> add(List<T> list, T element) {
		final ArrayList<T> result = new ArrayList<T>(list);
		result.add(element);
		return result;
	}

	public static <T> List<? extends T> replace(List<? extends T> list, T node,
			List<? extends T> replacements) {
		final ArrayList<T> result = new ArrayList<T>(list);
		result.remove(node);
		result.addAll(replacements);
		return result;
	}

	public static HasMbr findLeastIncreaseInMbrArea(Rectangle r,
			List<? extends HasMbr> list) {
		Preconditions.checkArgument(!list.isEmpty());
		Optional<Double> minDifference = Optional.absent();
		Optional<HasMbr> minDiffItem = Optional.absent();
		for (HasMbr m : list) {
			double diff = m.mbr().add(r).area() - m.mbr().area();
			if (!minDifference.isPresent() || (diff < minDifference.get())) {
				minDifference = Optional.of(diff);
				minDiffItem = Optional.of(m);
			}
		}
		return minDiffItem.get();
	}
}
