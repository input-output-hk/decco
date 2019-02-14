package io.iohk.decco.instances

import io.iohk.decco.PartialCodec

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
    new TraversableCodec[T, Traversable[T]](s"Traversable[${ev.typeCode}]")

  implicit def IterableInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Iterable[T]] =
    new TraversableCodec[T, Iterable[T]](s"Iterable[${ev.typeCode}]")

  implicit def SetInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Set[T]] =
    new TraversableCodec[T, Set[T]](s"Set[${ev.typeCode}]")

  implicit def SortedSetInstance[T: Ordering](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[SortedSet[T]] =
    new TraversableCodec[T, SortedSet[T]](s"SortedSet[${ev.typeCode}]")

  implicit def BitSetInstance(implicit intCodec: PartialCodec[Int]): PartialCodec[BitSet] =
    new TraversableCodec[Int, SortedSet[Int]]("BitSet")
      .mapExplicit(s"BitSet", sortedSet => BitSet(sortedSet.toSeq: _*), bitSet => bitSet)

  implicit def MapInstance[K: PartialCodec, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[Map[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](s"Map[${kvCodec.typeCode}]")
      .mapExplicit(s"Map[${kvCodec.typeCode}]", list => list.toMap, map => map.toList)

  implicit def SortedMapInstance[K: PartialCodec: Ordering, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[SortedMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](s"SortedMap[${kvCodec.typeCode}]")
      .mapExplicit(s"SortedMap[${kvCodec.typeCode}]", list => SortedMap(list: _*), map => map.toList)

  implicit def SeqInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Seq[T]] =
    new TraversableCodec[T, Seq[T]](s"Seq${ev.typeCode}")

  implicit def IndexedSeqInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[IndexedSeq[T]] =
    new TraversableCodec[T, IndexedSeq[T]](s"IndexedSeq${ev.typeCode}")

  implicit def LinearSeqInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[LinearSeq[T]] =
    new TraversableCodec[T, LinearSeq[T]](s"LinearSeq${ev.typeCode}")

  //
  // Concrete collection classes: Sets
  //
  implicit def HashSetInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[HashSet[T]] =
    new TraversableCodec[T, HashSet[T]](s"HashSet${ev.typeCode}")

  implicit def ListSetInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[ListSet[T]] =
    new TraversableCodec[T, ListSet[T]](s"ListSet${ev.typeCode}")

  implicit def TreeSetInstance[T: Ordering](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[TreeSet[T]] =
    new TraversableCodec[T, TreeSet[T]](s"TreeSet${ev.typeCode}")

  //
  // Concrete collection classes: IndexedSeqs
  //
  implicit def VectorInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Vector[T]] =
    new TraversableCodec[T, Vector[T]](s"Vector${ev.typeCode}")

  implicit def NumericRangeInstance[T: Integral](
      implicit intCodec: PartialCodec[Int],
      ev: PartialCodec[T],
      tupleCodec: PartialCodec[(Boolean, T, T, T)]
  ): PartialCodec[NumericRange[T]] = {
    tupleCodec.mapExplicit(
      s"NumericRange[${ev.typeCode}]",
      t => if (t._1) NumericRange.inclusive(t._2, t._3, t._4) else NumericRange(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def RangeInstance(
      implicit intCodec: PartialCodec[Int],
      tupleCodec: PartialCodec[(Boolean, Int, Int, Int)]
  ): PartialCodec[Range] = {
    tupleCodec.map(
      t => if (t._1) Range.inclusive(t._2, t._3, t._4) else Range(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def StringPartialCodec(implicit pf: PartialCodec[Array[Char]]): PartialCodec[String] =
    pf.map[String](String.valueOf, _.toCharArray)

  //
  // Concrete collection classes: LinearSeqs
  //
  implicit def ListInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[List[T]] =
    new TraversableCodec[T, List[T]](s"List${ev.typeCode}")

  implicit def StreamInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Stream[T]] =
    new TraversableCodec[T, Stream[T]](s"Stream${ev.typeCode}")

  implicit def QueueInstance[T](
      implicit ev: PartialCodec[T],
      intCodec: PartialCodec[Int]
  ): PartialCodec[Queue[T]] =
    new TraversableCodec[T, Queue[T]](s"Queue${ev.typeCode}")

  //
  // Concrete collection classes: Maps
  //
  implicit def HashMapInstance[K: PartialCodec, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[HashMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](s"HashMap[${kvCodec.typeCode}]")
      .mapExplicit(s"HashMap[${kvCodec.typeCode}]", list => HashMap(list: _*), map => map.toList)

  implicit def ListMapInstance[K: PartialCodec, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[ListMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](s"ListMap[${kvCodec.typeCode}]")
      .mapExplicit(s"ListMap[${kvCodec.typeCode}]", list => ListMap(list: _*), map => map.toList)

  implicit def TreeMapInstance[K: PartialCodec: Ordering, V: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[TreeMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]](s"TreeMap[${kvCodec.typeCode}]")
      .mapExplicit(s"TreeMap[${kvCodec.typeCode}]", list => TreeMap(list: _*), map => map.toList)

  //
  // Arrays
  //
  implicit def ArrayInstance[T: ClassTag](
      implicit ev: PartialCodec[T],
      iCodec: PartialCodec[Int]
  ): PartialCodec[Array[T]] =
    new TraversableCodec[T, Array[T]](s"Array[${ev.typeCode}]")
}

object CollectionInstances extends CollectionInstances
