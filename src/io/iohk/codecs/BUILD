load("//scala:rules.bzl", "scala_library", "scala_test")

scala_library(
    name = "codecs",
    deps = [],
    external = [
      "com.typesafe.akka:akka-actor%",
      "com.chuusai:shapeless%",
    ]
)

scala_test(
    name = "tests",
    deps = [ "codecs",
             "//src/io/iohk/codecs/nio/test/utils" ],
    external = [
      "org.scalacheck:scalacheck%",
      "org.mockito:mockito-core",
    ]
)
