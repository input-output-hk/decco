package io.iohk.decco

import io.iohk.decco.instances.{CollectionInstances, NativeInstances, OtherInstances, ProductInstances}

package object auto extends NativeInstances with CollectionInstances with ProductInstances with OtherInstances
