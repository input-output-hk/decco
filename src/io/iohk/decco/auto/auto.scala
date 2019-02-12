package io.iohk.decco

import io.iohk.decco.instances.{CollectionInstances, NativeInstances, ProductInstances}

package object auto extends NativeInstances with CollectionInstances with ProductInstances
