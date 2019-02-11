package io.iohk.decco

import io.iohk.decco.instances.{ArrayInstances, NativeInstances, ProductInstances}

package object auto extends NativeInstances with ArrayInstances with ProductInstances
