package com.github.davidmoten.rtree.fbs;

import java.util.List;

import com.github.davidmoten.rtree.Context;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.EntryDefault;
import com.github.davidmoten.rtree.Factory;
import com.github.davidmoten.rtree.Leaf;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.NonLeaf;
import com.github.davidmoten.rtree.NonLeafDefault;
import com.github.davidmoten.rtree.geometry.Geometry;

import rx.functions.Func1;

public class FactoryFlatBuffersDynamic<T, S extends Geometry> implements Factory<T, S> {
    private final Func1<T, byte[]> serializer;
    private final Func1<byte[], T> deserializer;

    public FactoryFlatBuffersDynamic(Func1<T, byte[]> serializer, Func1<byte[], T> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public Leaf<T, S> createLeaf(List<Entry<T, S>> entries, Context<T, S> context) {
        return new LeafFlatBuffersDynamic<T, S>(entries, context, serializer, deserializer);
    }

    @Override
    public NonLeaf<T, S> createNonLeaf(List<? extends Node<T, S>> children, Context<T, S> context) {
        return new NonLeafDefault<T, S>(children, context);
    }

    @Override
    public Entry<T, S> createEntry(T value, S geometry) {
        return EntryDefault.entry(value, geometry);
    }

}
