package com.github.davidmoten.rtree.geometry;

import com.github.davidmoten.util.ObjectsHelper;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class Circle implements Geometry {

	private final float x, y, radius;
	private final Rectangle mbr;

	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.mbr = Rectangle.create(x - radius, y - radius, x + radius, y
				+ radius);
	}

	public static Circle create(double x, double y, double radius) {
		return new Circle((float) x, (float) y, (float) radius);
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	@Override
	public Rectangle mbr() {
		return mbr;
	}

	@Override
	public double distance(Rectangle r) {
		return Math.max(0, new Point(x, y).distance(r) - radius);
	}

	@Override
	public boolean intersects(Rectangle r) {
		return new Point(x, y).distance(r) <= radius;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(x, y, radius);
	}

	@Override
	public boolean equals(Object obj) {
		Optional<Circle> other = ObjectsHelper.asClass(obj, Circle.class);
		if (other.isPresent()) {
			return Objects.equal(x, other.get().x)
					&& Objects.equal(y, other.get().y)
					&& Objects.equal(radius, other.get().radius);
		} else
			return false;
	}

}
