load("//scala:rules.bzl", "scala_library", "scala_test")

scala_library(
    name = "decco",
    external = ["com.chuusai:shapeless%"],
)

scala_test(
    name = "tests",
    external = [
        "org.mockito:mockito-core",
        "org.scalacheck:scalacheck%",
    ],
    deps = [
        "decco",
        "//src/io/iohk/codecs/nio/test/utils",
    ],
)
