package io.iohk.decco
package auto.instances

import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue, TreeMap, TreeSet}
import scala.collection.{BitSet, LinearSeq, SortedMap, SortedSet}

import scala.reflect.ClassTag

trait CollectionInstances {

  //
  // Collection interfaces
  //

  implicit def TraversableInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Traversable[T]] =
    new TraversableCodec[T, Traversable[T]]()

  implicit def IterableInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Iterable[T]] =
    new TraversableCodec[T, Iterable[T]]()

  implicit def SetInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Set[T]] =
    new TraversableCodec[T, Set[T]]()

  implicit def SortedSetInstance[T: Ordering](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[SortedSet[T]] =
    new TraversableCodec[T, SortedSet[T]]()

  implicit def BitSetInstance(implicit intCodec: Codec[Int]): Codec[BitSet] =
    new TraversableCodec[Int, SortedSet[Int]]()
      .map(sortedSet => BitSet(sortedSet.toSeq: _*), bitSet => bitSet)

  implicit def MapInstance[K: Codec, V: Codec](
      implicit intCodec: Codec[Int],
      kvCodec: Codec[(K, V)]
  ): Codec[Map[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]]()
      .map(list => list.toMap, map => map.toList)

  implicit def SortedMapInstance[K: Codec: Ordering, V: Codec](
      implicit intCodec: Codec[Int],
      kvCodec: Codec[(K, V)]
  ): Codec[SortedMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]]()
      .map(list => SortedMap(list: _*), map => map.toList)

  implicit def SeqInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Seq[T]] =
    new TraversableCodec[T, Seq[T]]()

  implicit def IndexedSeqInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[IndexedSeq[T]] =
    new TraversableCodec[T, IndexedSeq[T]]()

  implicit def LinearSeqInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[LinearSeq[T]] =
    new TraversableCodec[T, LinearSeq[T]]()

  //
  // Concrete collection classes: Sets
  //
  implicit def HashSetInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[HashSet[T]] =
    new TraversableCodec[T, HashSet[T]]()

  implicit def ListSetInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[ListSet[T]] =
    new TraversableCodec[T, ListSet[T]]()

  implicit def TreeSetInstance[T: Ordering](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[TreeSet[T]] =
    new TraversableCodec[T, TreeSet[T]]()

  //
  // Concrete collection classes: IndexedSeqs
  //
  implicit def VectorInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Vector[T]] =
    new TraversableCodec[T, Vector[T]]()

  implicit def NumericRangeInstance[T: Integral](
      implicit intCodec: Codec[Int],
      ev: Codec[T],
      tupleCodec: Codec[(Boolean, T, T, T)]
  ): Codec[NumericRange[T]] = {
    tupleCodec.map(
      t => if (t._1) NumericRange.inclusive(t._2, t._3, t._4) else NumericRange(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def RangeInstance(
      implicit intCodec: Codec[Int],
      tupleCodec: Codec[(Boolean, Int, Int, Int)]
  ): Codec[Range] = {
    tupleCodec.map(
      t => if (t._1) Range.inclusive(t._2, t._3, t._4) else Range(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def StringInstance(implicit pf: Codec[Array[Byte]]): Codec[String] =
    pf.map[String](bs => new String(bs, "UTF-8"), _.getBytes("UTF-8"))

  //
  // Concrete collection classes: LinearSeqs
  //
  implicit def ListInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[List[T]] =
    new TraversableCodec[T, List[T]]()

  implicit def StreamInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Stream[T]] =
    new TraversableCodec[T, Stream[T]]()

  implicit def QueueInstance[T](
      implicit ev: Codec[T],
      intCodec: Codec[Int]
  ): Codec[Queue[T]] =
    new TraversableCodec[T, Queue[T]]()

  //
  // Concrete collection classes: Maps
  //
  implicit def HashMapInstance[K: Codec, V: Codec](
      implicit intCodec: Codec[Int],
      kvCodec: Codec[(K, V)]
  ): Codec[HashMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]]()
      .map(list => HashMap(list: _*), map => map.toList)

  implicit def ListMapInstance[K: Codec, V: Codec](
      implicit intCodec: Codec[Int],
      kvCodec: Codec[(K, V)]
  ): Codec[ListMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]]()
      .map(list => ListMap(list: _*), map => map.toList)

  implicit def TreeMapInstance[K: Codec: Ordering, V: Codec](
      implicit intCodec: Codec[Int],
      kvCodec: Codec[(K, V)]
  ): Codec[TreeMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]]()
      .map(list => TreeMap(list: _*), map => map.toList)

  //
  // Arrays
  //
  implicit def ArrayInstance[T: ClassTag](
      implicit ev: Codec[T],
      iCodec: Codec[Int]
  ): Codec[Array[T]] =
    new TraversableCodec[T, Array[T]]()
}

object CollectionInstances extends CollectionInstances
