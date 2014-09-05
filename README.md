rtree
=========

In-memory immutable 2D [R-tree](http://en.wikipedia.org/wiki/R-tree) implementation in java using [RxJava Observables](https://github.com/ReactiveX/RxJava) for reactive processing of search results. 

Status: *released in Maven Central*

An [R-tree](http://en.wikipedia.org/wiki/R-tree) is a commonly used spatial index.

This was fun to make, has an elegant concise algorithm, is thread-safe and fast.

The algorithm to achieve immutability is cute. For insertion/deletion it involves recursion down to the 
required leaf node then recursion back up (using a stack built as we recurse down) to replace the parent nodes up to the root. The guts of 
it is in [Leaf.java](src/main/java/com/github/davidmoten/rtree/Leaf.java).

[Backpressure](https://github.com/ReactiveX/RxJava/wiki/Backpressure) support required some complexity because effectively a
bookmark needed to be kept for a position in the tree and returned to later to continue traversal. An immutable stack containing
 the node and child index of the path nodes came to the rescue here and recursion was abandoned in favour of looping to prevent stack overflow (unfortunately java doesn't support tail recursion!).

Continuous integration with Jenkins: <a href="https://xuml-tools.ci.cloudbees.com/"><img src="https://xuml-tools.ci.cloudbees.com/job/rtree/badge/icon"/></a>

Maven site reports are [here](http://davidmoten.github.io/rtree/index.html) including [javadoc](http://davidmoten.github.io/rtree/apidocs/index.html).

Features
------------
* immutable R-tree suitable for concurrency
* typed
* pluggable splitting heuristic ([```Splitter```](src/main/java/com/github/davidmoten/rtree/Splitter.java)). Default is [Guttman's quadratic split](http://www-db.deis.unibo.it/courses/SI-LS/papers/Gut84.pdf).
* pluggable insert heuristic ([```Selector```](src/main/java/com/github/davidmoten/rtree/Selector.java)). Default is least minimum bounding rectangle area increase.
* R*-tree heuristics available
* search returns [```Observable```](http://reactivex.io/RxJava/javadoc/rx/Observable.html) 
* search is cancelled by unsubscription
* over 250K inserts per second on i7 single thread into a tree with 10,000 entries
* search is ```O(log(n))``` on average
* insert, delete are ```O(n)``` worst case
* all search methods return lazy-evaluated streams offering a lot of flexibility and opportunity for functional composition and concurrency
* balanced delete
* supports [backpressure](https://github.com/ReactiveX/RxJava/wiki/Backpressure)
* JMH benchmarks

Number of points = 100, max children per node 4:

<img src="https://raw.githubusercontent.com/davidmoten/rtree/master/src/docs/rtree.png"/>

Getting started
----------------
Add this maven dependency to your pom.xml:

```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>rtree</artifactId>
  <version>0.1</version>
</dependency>
```

###Instantiate an R-Tree
Use the static builder methods on the ```RTree``` class:

```java
//create an R-tree with max children per node 32,
// min children 16 (the threshold at which members
// are redistributed)
RTree<String> tree = RTree.create();
```
You can specify a few parameters to the builder, including *minChildren*, *maxChildren*, *splitter*, *selector*:

```java
RTree<String> tree = RTree.minChildren(3).maxChildren(6).create();
```

###R*-tree
If you'd like an R*-tree (which uses a topological splitter on minimal overlap and a selector combination of minimal area increase and minimal overlap):

```
RTree<String> tree = RTree.star().maxChildren(6).create();
```

See benchmarks below for some of the performance differences.

###Add items to the R-tree
When you add an item to the R-tree you need to provide a geometry that represents the 2D physical location or 
extension of the item. The ``Geometries`` builder provides these factory methods:

* ```Geometries.rectangle```
* ```Geometries.circle```
* ```Geometries.point```

To add an item to an R-tree:

```java
RTree<T> tree = RTree.create();
tree = tree.add(item, Geometries.point(10,20));
```
or 
```java
tree = tree.add(Enry.entry(item, Geometries.point(10,20));
```

###Remove an item in the R-tree
To remove an item from an R-tree, you need to match the item and its geometry:

```java
tree = tree.delete(item, Geometries.point(10,20));
```
or 
```java
tree = tree.delete(entry);
```

###Custom geometries
You can also write your own implementation of [```Geometry```](src/main/java/com/github/davidmoten/rtree/geometry/Geometry.java). An implementation of ```Geometry``` needs to specify methods to:

* measure distance to a rectangle (0 means they intersect)
* provide a minimum bounding rectangle
* implement ```equals``` and ```hashCode``` for consistent equality checking

For the R-tree to be well-behaved, the distance function needs to satisfy these properties:

* ```distance(r) >= 0 for all rectangles r```
* ```if rectangle r1 contains r2 then distance(r1)<=distance(r2)```
* ```distance(r) = 0 if and only if the geometry intersects the rectangle r``` 

###Searching
The advantage of an R-tree is the ability to search for items in a region reasonably quickly. 
On average search is ```O(log(n))``` but worst case is ```O(n)```.

Search methods return ```Observable``` sequences:
```java
Observable<Entry<T>> results = tree.search(Geometries.rectangle(0,0,2,2));
```
or search for items within a distance from the given geometry:
```java
Observable<Entry<T>> results = tree.search(Geometries.rectangle(0,0,2,2),5.0);
```
or specify a predicate:
```java
Func1<Geometry,Boolean> function = ...
Observable<Entry<T>> results = tree.search(function);
```
To return all entries from an R-tree:
```java
Observable<Entry<T>> results = tree.entries();
```
or, using a predicate: 
```java
Observable<Entry<T>> results = tree.search(Functions.alwaysTrue());
```

Example
--------------
```java
import com.github.davidmoten.rtree.RTree;
import static com.github.davidmoten.rtree.geometry.Geometries.*;

RTree<String> tree = RTree.maxChildren(5).create();
tree = tree.add("DAVE", point(10, 20))
           .add("FRED", point(12, 25))
           .add("MARY", point(97, 125));
 
Observable<Entry<String>> entries = tree.search(Rectangle.create(8, 15, 30, 35));
```

###What do I do with the Observable thing?
Very useful, see [RxJava](http://github.com/ReactiveX/RxJava).

As an example, suppose you want to filter the search results then apply a function on each in parallel and reduce to some best answer:

```java
import rx.Observable;
import rx.functions.*;
import rx.schedulers.Schedulers;

Func1<Entry<String>, Character> firstCharacter = entry -> entry.value().charAt(0);
Func2<Character,Character,Character> firstAlphabetically = (x,y) -> x <=y ? x : y;

Character result = 
    tree.search(Geometries.rectangle(8, 15, 30, 35))
        // filter for names alphabetically less than M
        .filter(entry -> entry.value() < "M")
        // use a different scheduler for each entry
        .flatMap(entry -> Observable.just(entry).subscribeOn(Schedulers.computation())
        // get the first character of the name
        .map(entry -> firstCharacter(entry.value()))
        // reduce to the first character alphabetically 
        .reduce((x,y) -> firstAlphabetically(x,y))
        // subscribe to the stream and block for the result
        .toBlocking().single();
System.out.println(list);
```
output:
```
D
```

### How do I just get an Iterable back from a search?
If you are not familiar with the Observable API and want to skip the reactive stuff then here's how to get an ```Iterable``` from a search:

```java
Iterable<T> it = tree.search(Geometries.point(4,5)).toBlocking().toIterable();
```

How to build
----------------
```
git clone https://github.com/davidmoten/rtree.git
cd rtree
mvn clean install
```

How to run benchmarks
--------------------------
Benchmarks are provided by 
```
mvn clean install -Pbenchmark
```

### Results
```
Benchmark                                                                                      Mode  Samples       Score  Score error  Units
c.g.d.r.BenchmarksRTree.defaultRTreeInsertOneEntryInto100KEntriesMaxChildren10                thrpt       10  141390.437     2229.787  ops/s
c.g.d.r.BenchmarksRTree.defaultRTreeInsertOneEntryInto100KEntriesMaxChildren128               thrpt       10   32188.232     1394.730  ops/s
c.g.d.r.BenchmarksRTree.defaultRTreeInsertOneEntryInto100KEntriesMaxChildren32                thrpt       10   95012.263     1698.434  ops/s
c.g.d.r.BenchmarksRTree.defaultRTreeSearchOf100KPointsUsingSmallishRectangleMaxChildren10     thrpt       10   39589.974      380.340  ops/s
c.g.d.r.BenchmarksRTree.defaultRTreeSearchOf100KPointsUsingSmallishRectangleMaxChildren128    thrpt       10   12807.034      152.402  ops/s
c.g.d.r.BenchmarksRTree.defaultRTreeSearchOf100KPointsUsingSmallishRectangleMaxChildren32     thrpt       10   44671.270      623.509  ops/s
c.g.d.r.BenchmarksRTree.rStarTreeInsertOneEntryInto100KEntriesMaxChildren10                   thrpt       10   95635.212     2008.772  ops/s
c.g.d.r.BenchmarksRTree.rStarTreeInsertOneEntryInto100KEntriesMaxChildren128                  thrpt       10    4304.273       91.933  ops/s
c.g.d.r.BenchmarksRTree.rStarTreeInsertOneEntryInto100KEntriesMaxChildren32                   thrpt       10   34637.823      830.451  ops/s
c.g.d.r.BenchmarksRTree.rStarTreeSearchOf100KPointsUsingSmallishRectangleMaxChildren10        thrpt       10   33759.379      407.510  ops/s
c.g.d.r.BenchmarksRTree.rStarTreeSearchOf100KPointsUsingSmallishRectangleMaxChildren128       thrpt       10   23633.385      326.666  ops/s
c.g.d.r.BenchmarksRTree.rStarTreeSearchOf100KPointsUsingSmallishRectangleMaxChildren32        thrpt       10   66738.263     1307.356  ops/s
```
