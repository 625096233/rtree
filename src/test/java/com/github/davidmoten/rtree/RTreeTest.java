package com.github.davidmoten.rtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.rtree.geometry.Rectangle;

public class RTreeTest {

	private static final double PRECISION = 0.000001;

	@Test
	public void testInstantiation() {
		RTree<Object> tree = new RTree<Object>();
		assertTrue(tree.entries().isEmpty().toBlocking().single());
	}

	@Test
	public void testSearchEmptyTree() {
		RTree<Object> tree = new RTree<Object>();
		assertTrue(tree.search(r(1)).isEmpty().toBlocking().single());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchOnOneItem() {
		RTree<Object> tree = new RTree<Object>();
		Entry<Object> entry = new Entry<Object>(new Object(), r(1));
		tree = tree.add(entry);
		assertEquals(Arrays.asList(entry), tree.search(r(1)).toList()
				.toBlocking().single());
		System.out.println("entries="
				+ tree.entries().toList().toBlocking().single());
	}

	@Test
	public void testPerformanceAndEntriesCount() {

		long t = System.currentTimeMillis();
		int n = 10000;
		RTree<Object> tree = createRandomRTree(n);
		long diff = System.currentTimeMillis() - t;
		System.out.println("inserts/second = " + ((double) n / diff * 1000));
		assertEquals(n, (int) tree.entries().count().toBlocking().single());

		t = System.currentTimeMillis();
		Entry<Object> entry = tree.search(Rectangle.create(0, 0, 500, 500))
				.first().toBlocking().single();
		diff = System.currentTimeMillis() - t;
		System.out.println("found " + entry);
		System.out
				.println("time to get nearest with " + n + " entries=" + diff);

	}

	private static RTree<Object> createRandomRTree(int n) {
		RTree<Object> tree = RTree.builder().maxChildren(4).build();
		for (int i = 0; i < n; i++) {
			Entry<Object> entry = new Entry<Object>(new Object(), random());
			tree = tree.add(entry);
		}
		return tree;
	}

	@Test
	public void testNearest() {
		RTree<Object> tree = RTree.builder().maxChildren(4).build().add(e(1))
				.add(e(2)).add(e(10)).add(e(11));
		List<Entry<Object>> list = tree.nearest(r(9), 10, 2).toList()
				.toBlocking().single();
		assertEquals(2, list.size());
		assertEquals(10, list.get(0).mbr().x1(), PRECISION);
		assertEquals(11, list.get(1).mbr().x1(), PRECISION);
	}

	@Test
	public void testVisualizer() {
		RTree<Object> tree = createRandomRTree(100);
		tree.visualize(600, 600, new Rectangle(-20, -20, 1100, 1100), 5).save(
				new File("target/tree.png"), "PNG");
	}

	@Test
	public void testDeleteOneFromOne() {
		Entry<Object> e1 = e(1);
		RTree<Object> tree = RTree.builder().maxChildren(4).build().add(e1)
				.delete(e1);
		assertEquals(0, (int) tree.entries().count().toBlocking().single());
	}

	@Test
	public void testDeleteOneFromTreeWithDepthGreaterThanOne() {
		Entry<Object> e1 = e(1);
		RTree<Object> tree = RTree.builder().maxChildren(4).build().add(e1)
				.add(e(2)).add(e(3)).add(e(4)).add(e(5)).add(e(6)).add(e(7))
				.add(e(8)).add(e(9)).add(e(10)).delete(e1);
		assertEquals(9, (int) tree.entries().count().toBlocking().single());
	}

	private static Entry<Object> e(int n) {
		return new Entry<Object>(new Object(), r(n));
	}

	private static Rectangle r(int n) {
		return Rectangle.create(n, n, n + 1, n + 1);
	}

	private static Rectangle r(int n, int m) {
		return Rectangle.create(n, m, n + 1, m + 1);
	}

	private static Rectangle random() {
		return r((int) Math.round(Math.sqrt(Math.random()) * 1000),
				(int) Math.round(Math.sqrt(Math.random()) * 1000));
	}
}
