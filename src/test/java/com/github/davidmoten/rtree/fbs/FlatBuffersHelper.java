package com.github.davidmoten.rtree.fbs;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.EntryDefault;
import com.github.davidmoten.rtree.Util;
import com.github.davidmoten.rtree.flatbuffers.Box_;
import com.github.davidmoten.rtree.flatbuffers.Entry_;
import com.github.davidmoten.rtree.flatbuffers.GeometryType_;
import com.github.davidmoten.rtree.flatbuffers.Geometry_;
import com.github.davidmoten.rtree.flatbuffers.Node_;
import com.github.davidmoten.rtree.flatbuffers.Point_;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.flatbuffers.FlatBufferBuilder;

import rx.functions.Func1;

class FlatBuffersHelper {

    static <T, S extends Geometry> int addEntries(List<Entry<T, S>> entries,
            FlatBufferBuilder builder, Func1<T, byte[]> serializer) {
        int[] entries2 = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            Geometry g = entries.get(i).geometry();
            final int geom;
            final byte geomType;
            if (g instanceof Rectangle) {
                Rectangle b = (Rectangle) g;
                geom = Box_.createBox_(builder, b.x1(), b.y1(), b.x2(), b.y2());
                geomType = GeometryType_.Box;
            } else if (g instanceof Point) {
                Point p = (Point) g;
                geom = Point_.createPoint_(builder, p.x(), p.y());
                geomType = GeometryType_.Point;
            } else
                throw new RuntimeException("unexpected");

            Geometry_.startGeometry_(builder);
            if (geomType == GeometryType_.Box)
                Geometry_.addBox(builder, geom);
            else if (geomType == GeometryType_.Point)
                Geometry_.addPoint(builder, geom);
            else
                throw new RuntimeException("unexpected");

            Geometry_.addType(builder, geomType);
            int geo = Geometry_.endGeometry_(builder);
            int obj = Entry_.createObjectVector(builder, serializer.call(entries.get(i).value()));
            entries2[i] = Entry_.createEntry_(builder, geo, obj);
        }
        int ents = Node_.createEntriesVector(builder, entries2);
        Rectangle mbb = Util.mbr(entries);
        int b = Box_.createBox_(builder, mbb.x1(), mbb.y1(), mbb.x2(), mbb.y2());
        Node_.startNode_(builder);
        Node_.addMbb(builder, b);
        Node_.addEntries(builder, ents);
        return Node_.endNode_(builder);
    }

    static <T, S extends Geometry> List<Entry<T, S>> createEntries(Node_ node) {
        List<Entry<T, S>> list = new ArrayList<Entry<T, S>>(node.entriesLength());
        for (int i = 0; i < node.entriesLength(); i++) {
            Geometry_ g = node.entries(i).geometry();
            final Geometry geometry;
            if (g.type() == GeometryType_.Box) {
                Box_ b = g.box();
                geometry = Rectangle.create(b.minX(), b.minY(), b.maxX(), b.maxY());
            } else if (g.type() == GeometryType_.Point) {
                Point_ p = g.point();
                geometry = Point.create(p.x(), p.y());
            } else
                throw new RuntimeException("unexpected");
            node.entries(i).object(i);
            list.add(EntryDefault.<T, S> entry((T) new Object(), (S) geometry));
        }
        return list;
    }

}