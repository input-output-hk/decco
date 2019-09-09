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
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Traversable[T]] =
    new TraversableCodecContract[T, Traversable[T]]()

  implicit def IterableInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Iterable[T]] =
    new TraversableCodecContract[T, Iterable[T]]()

  implicit def SetInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Set[T]] =
    new TraversableCodecContract[T, Set[T]]()

  implicit def SortedSetInstance[T: Ordering](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[SortedSet[T]] =
    new TraversableCodecContract[T, SortedSet[T]]()

  implicit def BitSetInstance(implicit intCodec: CodecContract[Int]): CodecContract[BitSet] =
    new TraversableCodecContract[Int, SortedSet[Int]]()
      .map(sortedSet => BitSet(sortedSet.toSeq: _*), bitSet => bitSet)

  implicit def MapInstance[K: CodecContract, V: CodecContract](
      implicit intCodec: CodecContract[Int],
      kvCodec: CodecContract[(K, V)]
  ): CodecContract[Map[K, V]] =
    new TraversableCodecContract[(K, V), List[(K, V)]]()
      .map(list => list.toMap, map => map.toList)

  implicit def SortedMapInstance[K: CodecContract: Ordering, V: CodecContract](
      implicit intCodec: CodecContract[Int],
      kvCodec: CodecContract[(K, V)]
  ): CodecContract[SortedMap[K, V]] =
    new TraversableCodecContract[(K, V), List[(K, V)]]()
      .map(list => SortedMap(list: _*), map => map.toList)

  implicit def SeqInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Seq[T]] =
    new TraversableCodecContract[T, Seq[T]]()

  implicit def IndexedSeqInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[IndexedSeq[T]] =
    new TraversableCodecContract[T, IndexedSeq[T]]()

  implicit def LinearSeqInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[LinearSeq[T]] =
    new TraversableCodecContract[T, LinearSeq[T]]()

  //
  // Concrete collection classes: Sets
  //
  implicit def HashSetInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[HashSet[T]] =
    new TraversableCodecContract[T, HashSet[T]]()

  implicit def ListSetInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[ListSet[T]] =
    new TraversableCodecContract[T, ListSet[T]]()

  implicit def TreeSetInstance[T: Ordering](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[TreeSet[T]] =
    new TraversableCodecContract[T, TreeSet[T]]()

  //
  // Concrete collection classes: IndexedSeqs
  //
  implicit def VectorInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Vector[T]] =
    new TraversableCodecContract[T, Vector[T]]()

  implicit def NumericRangeInstance[T: Integral](
      implicit intCodec: CodecContract[Int],
      ev: CodecContract[T],
      tupleCodec: CodecContract[(Boolean, T, T, T)]
  ): CodecContract[NumericRange[T]] = {
    tupleCodec.map(
      t => if (t._1) NumericRange.inclusive(t._2, t._3, t._4) else NumericRange(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def RangeInstance(
      implicit intCodec: CodecContract[Int],
      tupleCodec: CodecContract[(Boolean, Int, Int, Int)]
  ): CodecContract[Range] = {
    tupleCodec.map(
      t => if (t._1) Range.inclusive(t._2, t._3, t._4) else Range(t._2, t._3, t._4),
      range => (range.isInclusive, range.start, range.end, range.step)
    )
  }

  implicit def StringInstance(implicit pf: CodecContract[Array[Byte]]): CodecContract[String] =
    pf.map[String](bs => new String(bs, "UTF-8"), _.getBytes("UTF-8"))

  //
  // Concrete collection classes: LinearSeqs
  //
  implicit def ListInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[List[T]] =
    new TraversableCodecContract[T, List[T]]()

  implicit def StreamInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Stream[T]] =
    new TraversableCodecContract[T, Stream[T]]()

  implicit def QueueInstance[T](
      implicit ev: CodecContract[T],
      intCodec: CodecContract[Int]
  ): CodecContract[Queue[T]] =
    new TraversableCodecContract[T, Queue[T]]()

  //
  // Concrete collection classes: Maps
  //
  implicit def HashMapInstance[K: CodecContract, V: CodecContract](
      implicit intCodec: CodecContract[Int],
      kvCodec: CodecContract[(K, V)]
  ): CodecContract[HashMap[K, V]] =
    new TraversableCodecContract[(K, V), List[(K, V)]]()
      .map(list => HashMap(list: _*), map => map.toList)

  implicit def ListMapInstance[K: CodecContract, V: CodecContract](
      implicit intCodec: CodecContract[Int],
      kvCodec: CodecContract[(K, V)]
  ): CodecContract[ListMap[K, V]] =
    new TraversableCodecContract[(K, V), List[(K, V)]]()
      .map(list => ListMap(list: _*), map => map.toList)

  implicit def TreeMapInstance[K: CodecContract: Ordering, V: CodecContract](
      implicit intCodec: CodecContract[Int],
      kvCodec: CodecContract[(K, V)]
  ): CodecContract[TreeMap[K, V]] =
    new TraversableCodecContract[(K, V), List[(K, V)]]()
      .map(list => TreeMap(list: _*), map => map.toList)

  //
  // Arrays
  //
  implicit def ArrayInstance[T: ClassTag](
      implicit ev: CodecContract[T],
      iCodec: CodecContract[Int]
  ): CodecContract[Array[T]] =
    new TraversableCodecContract[T, Array[T]]()
}

object CollectionInstances extends CollectionInstances
