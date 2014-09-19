package com.github.davidmoten.rtree;

import com.google.common.base.Preconditions;

/**
 * Configures an RTree prior to instantiation of an {@link RTree}.
 */
public final class Context {

    private final int maxChildren;
    private final int minChildren;
    private final Splitter splitter;
    private final Selector selector;

    /**
     * Constructor.
     * 
     * @param minChildren
     *            minimum number of children per node
     * @param maxChildren
     *            max number of children per node
     * @param selector
     *            algorithm to select search path
     * @param splitter
     *            algorithm to split the children across two new nodes
     */
    public Context(int minChildren, int maxChildren, Selector selector, Splitter splitter) {
        Preconditions.checkNotNull(splitter);
        Preconditions.checkArgument(maxChildren > 2);
        Preconditions.checkArgument(minChildren >= 1);
        this.selector = selector;
        this.maxChildren = maxChildren;
        this.minChildren = minChildren;
        this.splitter = splitter;
    }

    public int maxChildren() {
        return maxChildren;
    }

    public int minChildren() {
        return minChildren;
    }

    public Splitter splitter() {
        return splitter;
    }

    public Selector selector() {
        return selector;
    }

}
