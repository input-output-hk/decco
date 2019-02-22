package io.iohk.decco.instances

import io.iohk.decco.TypeCode._
import io.iohk.decco.{PartialCodec, TypeCode}

import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue, TreeMap, TreeSet}
import scala.collection.{BitSet, LinearSeq, SortedMap, SortedSet}
import scala.reflect.ClassTag

trait CollectionInstances {

  //
  // Collection interfaces
  //

  implicit def TraversableInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Traversable[T]] =
    new TraversableCodec[T, Traversable[T]](genTypeCode[Traversable, T])

  implicit def IterableInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Iterable[T]] =
    new TraversableCodec[T, Iterable[T]](genTypeCode[Iterable, T])

  implicit def SetInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Set[T]] =
    new TraversableCodec[T, Set[T]](genTypeCode[Set, T])

  implicit def SortedSetInstance[T: Ordering](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[SortedSet[T]] =
    new TraversableCodec[T, SortedSet[T]](genTypeCode[SortedSet, T])

  implicit def BitSetInstance(implicit intCodec: PartialCodec[Int]): PartialCodec[BitSet] =
    new TraversableCodec[Int, SortedSet[Int]](genTypeCode[SortedSet, Int])
      .map(TypeCode[BitSet], sortedSet => BitSet(sortedSet.toSeq: _*), bitSet => bitSet)

  implicit def MapInstance[K: PartialCodec, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[Map[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](genTypeCode[List, (K, V)])
      .map(genTypeCode[Map, K, V], list => list.toMap, map => map.toList)

  implicit def SortedMapInstance[K: PartialCodec: Ordering, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[SortedMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](genTypeCode[List, (K, V)])
      .map(genTypeCode[SortedMap, K, V], list => SortedMap(list: _*), map => map.toList)

  implicit def SeqInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Seq[T]] =
    new TraversableCodec[T, Seq[T]](genTypeCode[Seq, T])

  implicit def IndexedSeqInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[IndexedSeq[T]] =
    new TraversableCodec[T, IndexedSeq[T]](genTypeCode[IndexedSeq, T])

  implicit def LinearSeqInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[LinearSeq[T]] =
    new TraversableCodec[T, LinearSeq[T]](genTypeCode[LinearSeq, T])

  //
  // Concrete collection classes: Sets
  //
  implicit def HashSetInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[HashSet[T]] =
    new TraversableCodec[T, HashSet[T]](genTypeCode[HashSet, T])

  implicit def ListSetInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[ListSet[T]] =
    new TraversableCodec[T, ListSet[T]](genTypeCode[ListSet, T])

  implicit def TreeSetInstance[T: Ordering](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[TreeSet[T]] =
    new TraversableCodec[T, TreeSet[T]](genTypeCode[TreeSet, T])

  //
  // Concrete collection classes: IndexedSeqs
  //
  implicit def VectorInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Vector[T]] =
    new TraversableCodec[T, Vector[T]](genTypeCode[Vector, T])

  implicit def NumericRangeInstance[T: Integral](
      implicit intCodec: PartialCodec[Int],
      ev: PartialCodec[T],
      tupleCodec: PartialCodec[(Boolean, T, T, T)]
  ): PartialCodec[NumericRange[T]] = {
    tupleCodec.map(
      genTypeCode[NumericRange, T],
      t => if (t._1) NumericRange.inclusive(t._2, t._3, t._4) else NumericRange(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def RangeInstance(
      implicit intCodec: PartialCodec[Int],
      tupleCodec: PartialCodec[(Boolean, Int, Int, Int)]
  ): PartialCodec[Range] = {
    tupleCodec.map(
      TypeCode[Range],
      t => if (t._1) Range.inclusive(t._2, t._3, t._4) else Range(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def StringPartialCodec(implicit pf: PartialCodec[Array[Char]]): PartialCodec[String] =
    pf.map[String](TypeCode[String], String.valueOf, _.toCharArray)

  //
  // Concrete collection classes: LinearSeqs
  //
  implicit def ListInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[List[T]] =
    new TraversableCodec[T, List[T]](genTypeCode[List, T])

  implicit def StreamInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Stream[T]] =
    new TraversableCodec[T, Stream[T]](genTypeCode[Stream, T])

  implicit def QueueInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Queue[T]] =
    new TraversableCodec[T, Queue[T]](genTypeCode[Queue, T])

  //
  // Concrete collection classes: Maps
  //
  implicit def HashMapInstance[K: PartialCodec, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[HashMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](genTypeCode[List, (K, V)])
      .map(genTypeCode[HashMap, K, V], list => HashMap(list: _*), map => map.toList)

  implicit def ListMapInstance[K: PartialCodec, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[ListMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](genTypeCode[List, (K, V)])
      .map(genTypeCode[ListMap, K, V], list => ListMap(list: _*), map => map.toList)

  implicit def TreeMapInstance[K: PartialCodec: Ordering, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[TreeMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](genTypeCode[List, (K, V)])
      .map(genTypeCode[TreeMap, K, V], list => TreeMap(list: _*), map => map.toList)

  //
  // Arrays
  //
  implicit def ArrayInstance[T: ClassTag](
      implicit ev: PartialCodec[T],
      iCodec: PartialCodec[Int]
  ): PartialCodec[Array[T]] =
    new TraversableCodec[T, Array[T]](genTypeCode[Array, T])
}

object CollectionInstances extends CollectionInstances
