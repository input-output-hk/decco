package io.iohk.decco

import auto.instances._

package object auto extends HighPriorityInstances

trait HighPriorityInstances extends NativeInstances with OtherInstances with MiddlePriorityInstances
trait MiddlePriorityInstances extends CollectionInstances with LowPriorityInstances

trait LowPriorityInstances extends ProductInstances
