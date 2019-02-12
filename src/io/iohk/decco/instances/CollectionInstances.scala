package io.iohk.decco.instances

import io.iohk.decco.PartialCodec

import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue, TreeMap, TreeSet}
import scala.collection.{BitSet, LinearSeq, SortedMap, SortedSet}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

trait CollectionInstances {

  //
  // Collection interfaces
  //

  implicit def TraversableInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Traversable[T]] =
    new TraversableCodec[T, Traversable[T]]

  implicit def IterableInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Iterable[T]] =
    new TraversableCodec[T, Iterable[T]]

  implicit def SetInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Set[T]] =
    new TraversableCodec[T, Set[T]]

  implicit def SortedSetInstance[T: ClassTag: TypeTag: PartialCodec: Ordering](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[SortedSet[T]] =
    new TraversableCodec[T, SortedSet[T]]

  implicit def BitSetInstance(implicit intCodec: PartialCodec[Int]): PartialCodec[BitSet] =
    new TraversableCodec[Int, SortedSet[Int]].map(sortedSet => BitSet(sortedSet.toSeq: _*), bitSet => bitSet)

  implicit def MapInstance[K: ClassTag: TypeTag: PartialCodec, V: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[Map[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]].map(list => list.toMap, map => map.toList)

  implicit def SortedMapInstance[K: ClassTag: TypeTag: PartialCodec: Ordering, V: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[SortedMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]].map(list => SortedMap(list: _*), map => map.toList)

  implicit def SeqInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Seq[T]] =
    new TraversableCodec[T, Seq[T]]()

  implicit def IndexedSeqInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[IndexedSeq[T]] =
    new TraversableCodec[T, IndexedSeq[T]]()

  implicit def LinearSeqInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[LinearSeq[T]] =
    new TraversableCodec[T, LinearSeq[T]]()

  //
  // Concrete collection classes: Sets
  //
  implicit def HashSetInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[HashSet[T]] =
    new TraversableCodec[T, HashSet[T]]

  implicit def ListSetInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[ListSet[T]] =
    new TraversableCodec[T, ListSet[T]]

  implicit def TreeSetInstance[T: ClassTag: TypeTag: PartialCodec: Ordering](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[TreeSet[T]] =
    new TraversableCodec[T, TreeSet[T]]

  //
  // Concrete collection classes: IndexedSeqs
  //
  implicit def VectorInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Vector[T]] =
    new TraversableCodec[T, Vector[T]]

  implicit def NumericRangeInstance[T: ClassTag: TypeTag: PartialCodec: Integral](
      implicit intCodec: PartialCodec[Int],
      tupleCodec: PartialCodec[(Boolean, T, T, T)]
  ): PartialCodec[NumericRange[T]] = {
    tupleCodec.map(
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

  // String included with natives

  //
  // Concrete collection classes: LinearSeqs
  //
  implicit def ListInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[List[T]] =
    new TraversableCodec[T, List[T]]

  implicit def StreamInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Stream[T]] =
    new TraversableCodec[T, Stream[T]]

  implicit def QueueInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int]
  ): PartialCodec[Queue[T]] =
    new TraversableCodec[T, Queue[T]]

  //
  // Concrete collection classes: Maps
  //
  implicit def HashMapInstance[K: ClassTag: TypeTag: PartialCodec, V: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[HashMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]].map(list => HashMap(list: _*), map => map.toList)

  implicit def ListMapInstance[K: ClassTag: TypeTag: PartialCodec, V: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[ListMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]].map(list => ListMap(list: _*), map => map.toList)

  implicit def TreeMapInstance[K: ClassTag: TypeTag: PartialCodec: Ordering, V: ClassTag: TypeTag: PartialCodec](
      implicit intCodec: PartialCodec[Int],
      kvCodec: PartialCodec[(K, V)]
  ): PartialCodec[TreeMap[K, V]] =
    new TraversableCodec[(K, V), List[(K, V)]].map(list => TreeMap(list: _*), map => map.toList)

  //
  // Arrays
  //
  implicit def ArrayInstance[T: ClassTag: TypeTag: PartialCodec](
      implicit iCodec: PartialCodec[Int]
  ): PartialCodec[Array[T]] =
    new TraversableCodec[T, Array[T]]
}

object CollectionInstances extends CollectionInstances
