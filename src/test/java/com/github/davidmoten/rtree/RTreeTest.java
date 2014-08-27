package com.github.davidmoten.rtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class RTreeTest {

	@Test
	public void testInstantiation() {
		RTree tree = new RTree();
		assertTrue(tree.entries().isEmpty().toBlocking().single());
	}

	@Test
	public void testSearchEmptyTree() {
		RTree tree = new RTree();
		assertTrue(tree.search(r(1)).isEmpty().toBlocking().single());
	}

	@Test
	public void testSearchOnOneItem() {
		RTree tree = new RTree();
		Entry entry = new Entry(new Object(), r(1));
		tree = tree.add(entry);
		assertEquals(Arrays.asList(entry), tree.search(r(1)).toList()
				.toBlocking().single());
		System.out.println("entries="
				+ tree.entries().toList().toBlocking().single());
	}

	@Test
	public void testPerformance() {
		long t = System.currentTimeMillis();
		RTree tree = new RTree();
		int n = 10000;
		for (int i = 0; i < n; i++) {
			Entry entry = new Entry(new Object(), random());
			tree = tree.add(entry);
		}
		long diff = System.currentTimeMillis() - t;
		System.out.println("inserts/second = " + ((double) n / diff * 1000));
		assertEquals(n, (int) tree.entries().count().toBlocking().single());
	}

	private static Rectangle r(int n) {
		return Rectangle.create(n, n, n + 1, n + 1);
	}

	private static Rectangle r(int n, int m) {
		return Rectangle.create(n, m, n + 1, m + 1);
	}

	private static Rectangle random() {
		return r((int) Math.round(Math.random() * 1000),
				(int) Math.round(Math.random() * 1000));
	}

}
