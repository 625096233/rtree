package com.github.davidmoten.rtree;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.util.ListPair;
import com.github.davidmoten.util.Pair;
import com.google.common.collect.Sets;

public class QuadraticSplitterTest {

    @Test
    public void testWorstCombinationOn3() {
        final Mbr r1 = r(1);
        final Mbr r2 = r(100);
        final Mbr r3 = r(3);
        final Pair<Mbr> pair = QuadraticSplitter.worstCombination(Arrays.asList(r1, r2, r3));
        assertEquals(r1, pair.value1());
        assertEquals(r2, pair.value2());
    }

    @Test
    public void testWorstCombinationOnTwoEntries() {
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final Pair<Mbr> pair = QuadraticSplitter.worstCombination(Arrays.asList(r1, r2));
        assertEquals(r1, pair.value1());
        assertEquals(r2, pair.value2());
    }

    @Test
    public void testWorstCombinationOn4() {
        final Mbr r1 = r(2);
        final Mbr r2 = r(1);
        final Mbr r3 = r(3);
        final Mbr r4 = r(4);
        final Pair<Mbr> pair = QuadraticSplitter.worstCombination(Arrays.asList(r1, r2, r3, r4));
        assertEquals(r2, pair.value1());
        assertEquals(r4, pair.value2());
    }

    @Test
    public void testGetBestCandidateForGroup1() {
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final List<Mbr> list = Arrays.asList(r1);
        final List<Mbr> group = Arrays.asList(r2);
        final Mbr r = QuadraticSplitter.getBestCandidateForGroup(list, group, Util.mbr(group));
        assertEquals(r1, r);
    }

    @Test
    public void testGetBestCandidateForGroup2() {
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final Mbr r3 = r(10);
        final List<Mbr> list = Arrays.asList(r1);
        final List<Mbr> group = Arrays.asList(r2, r3);
        final Mbr r = QuadraticSplitter.getBestCandidateForGroup(list, group, Util.mbr(group));
        assertEquals(r1, r);
    }

    @Test
    public void testGetBestCandidateForGroup3() {
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final Mbr r3 = r(10);
        final List<Mbr> list = Arrays.asList(r1, r2);
        final List<Mbr> group = Arrays.asList(r3);
        final Mbr r = QuadraticSplitter.getBestCandidateForGroup(list, group, Util.mbr(group));
        assertEquals(r2, r);
    }

    @Test
    public void testSplit() {
        final QuadraticSplitter q = new QuadraticSplitter();
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final Mbr r3 = r(100);
        final Mbr r4 = r(101);
        final ListPair<Mbr> pair = q.split(Arrays.asList(r1, r2, r3, r4));
        assertEquals(Sets.newHashSet(r1, r2), Sets.newHashSet(pair.list1()));
        assertEquals(Sets.newHashSet(r3, r4), Sets.newHashSet(pair.list2()));
    }

    @Test
    public void testSplit2() {
        final QuadraticSplitter q = new QuadraticSplitter();
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final Mbr r3 = r(100);
        final Mbr r4 = r(101);
        final Mbr r5 = r(103);
        final ListPair<Mbr> pair = q.split(Arrays.asList(r1, r2, r3, r4, r5));
        assertEquals(Sets.newHashSet(r1, r2), Sets.newHashSet(pair.list1()));
        assertEquals(Sets.newHashSet(r3, r4, r5), Sets.newHashSet(pair.list2()));
    }

    @Test
    public void testSplit3() {
        final QuadraticSplitter q = new QuadraticSplitter();
        final Mbr r1 = r(1);
        final Mbr r2 = r(2);
        final Mbr r3 = r(100);
        final Mbr r4 = r(101);
        final Mbr r5 = r(103);
        final Mbr r6 = r(104);
        final ListPair<Mbr> pair = q.split(Arrays.asList(r1, r2, r3, r4, r5, r6));
        assertEquals(Sets.newHashSet(r1, r2, r3), Sets.newHashSet(pair.list1()));
        assertEquals(Sets.newHashSet(r4, r5, r6), Sets.newHashSet(pair.list2()));
    }

    private static Mbr r(int n) {
        return new Mbr(Rectangle.create(n, n, n + 1, n + 1));
    }

}
