package com.github.davidmoten.rtree.fbs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.rtree.Context;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.NonLeaf;
import com.github.davidmoten.rtree.fbs.generated.Box_;
import com.github.davidmoten.rtree.fbs.generated.Entry_;
import com.github.davidmoten.rtree.fbs.generated.GeometryType_;
import com.github.davidmoten.rtree.fbs.generated.Geometry_;
import com.github.davidmoten.rtree.fbs.generated.Node_;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.internal.NodeAndEntries;

import rx.Subscriber;
import rx.functions.Func1;

final class NodeFlatBuffers<T, S extends Geometry> implements NonLeaf<T, S> {

    private final Node_ node;
    private final Context<T, S> context;
    private final Func1<byte[], T> deserializer;

    NodeFlatBuffers(Node_ node, Context<T, S> context, Func1<byte[], T> deserializer) {
        Preconditions.checkArgument(node.childrenLength() > 0 || node.entriesLength() > 0);
        this.node = node;
        this.context = context;
        this.deserializer = deserializer;
    }

    @Override
    public List<Node<T, S>> add(Entry<? extends T, ? extends S> entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeAndEntries<T, S> delete(Entry<? extends T, ? extends S> entry, boolean all) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void searchWithoutBackpressure(Func1<? super Geometry, Boolean> criterion,
            Subscriber<? super Entry<T, S>> subscriber) {
        // pass through entry and geometry and box instances to be reused for
        // flatbuffers extraction this reduces allocation/gc costs (but of
        // course introduces some mutable ugliness into the codebase)
        search(node, criterion, subscriber, deserializer, new Entry_(), new Geometry_(),
                new Box_());
    }

    @SuppressWarnings("unchecked")
    private static <T, S extends Geometry> void search(Node_ node,
            Func1<? super Geometry, Boolean> criterion, Subscriber<? super Entry<T, S>> subscriber,
            Func1<byte[], T> deserializer, Entry_ entry, Geometry_ geometry, Box_ box) {
        {
            node.mbb(box);
            if (!criterion
                    .call(Geometries.rectangle(box.minX(), box.minY(), box.maxX(), box.maxY())))
                return;
        }
        int numChildren = node.childrenLength();
        // reduce allocations by reusing objects
        Node_ child = new Node_();
        if (numChildren > 0) {
            for (int i = 0; i < numChildren; i++) {
                if (subscriber.isUnsubscribed())
                    return;
                node.children(child, i);
                search(child, criterion, subscriber, deserializer, entry, geometry, box);
            }
        } else {
            int numEntries = node.entriesLength();
            // reduce allocations by reusing objects
            // check all entries
            for (int i = 0; i < numEntries; i++) {
                if (subscriber.isUnsubscribed())
                    return;
                // set entry
                node.entries(entry, i);
                // set geometry
                entry.geometry(geometry);
                final Geometry g = toGeometry(geometry);
                if (criterion.call(g)) {
                    ByteBuffer bb = entry.objectAsByteBuffer();
                    byte[] bytes = Arrays.copyOfRange(bb.array(), bb.position(), bb.limit());
                    T t = deserializer.call(bytes);
                    subscriber.onNext(Entries.entry(t, (S) g));
                }
            }
        }

    }

    private List<Node<T, S>> createChildren() {
        List<Node<T, S>> children = new ArrayList<Node<T, S>>(node.childrenLength());
        // reduce allocations by resusing objects
        int numChildren = node.childrenLength();
        for (int i = 0; i < numChildren; i++) {
            Node_ child = node.children(i);
            children.add(new NodeFlatBuffers<T, S>(child, context, deserializer));
        }
        return children;
    }

    @Override
    public int count() {
        if (node.childrenLength() > 0)
            return node.childrenLength();
        else
            return node.entriesLength();
    }

    @Override
    public Context<T, S> context() {
        return context;
    }

    @Override
    public Geometry geometry() {
        return createBox(node.mbb());
    }

    private static Geometry createBox(Box_ b) {
        return Rectangle.create(b.minX(), b.minY(), b.maxX(), b.maxY());
    }

    @Override
    public List<? extends Node<T, S>> children() {
        return createChildren();
    }

    @SuppressWarnings("unchecked")
    private static <S extends Geometry> S toGeometry(Geometry_ g) {
        final Geometry result;
        byte type = g.type();
        if (type == GeometryType_.Box) {
            result = createBox(g.box());
        } else if (type == GeometryType_.Point) {
            result = Point.create(g.point().x(), g.point().y());
        } else
            throw new UnsupportedOperationException();
        return (S) result;
    }

    @Override
    public String toString() {
        return "Node [" + (node.childrenLength() > 0 ? "NonLeaf" : "Leaf") + ","
                + createBox(node.mbb()).toString() + "]";
    }

}
