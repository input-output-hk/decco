package io.iohk.decco

import auto.instances._
import io.iohk.decco.Codec.Failure

package object auto extends HighPriorityInstances {
  implicit def codecContract2Codec[T](implicit codecContract: CodecContract[T]): Codec[T] = new Codec[T] {
    override def encode[B](t: T)(implicit bi: BufferInstantiator[B]): B = codecContract.encode(t)

    override def decode[B](start: Int, source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T] =
      codecContract.decode(start, source)

    override def decode[B](source: B)(implicit bi: BufferInstantiator[B]): Either[Failure, T] =
      codecContract.decode(source)
  }
}

package object definitions extends HighPriorityInstances

trait HighPriorityInstances extends NativeInstances with OtherInstances with MiddlePriorityInstances
trait MiddlePriorityInstances extends CollectionInstances with LowPriorityInstances

trait LowPriorityInstances extends ProductInstances
