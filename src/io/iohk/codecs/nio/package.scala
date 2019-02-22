package io.iohk.codecs

import io.iohk.codecs.nio.components._

package object nio extends NioCodecs {

  object auto extends NativeCodecs with ProductCodecs with OtherCodecs with CoproductCodecs
}
