load("//scala:rules.bzl", "scala_library", "scala_test")

scala_library(
    name = "decco",
    exports = ["//3rdparty/jvm/com/chuusai:shapeless"],
    deps = ["//3rdparty/jvm/com/chuusai:shapeless"],
)

scala_test(
    name = "tests",
    deps = [
        "decco",
        "//3rdparty/jvm/org/mockito:mockito_core",
        "//3rdparty/jvm/org/scalacheck",
        "//src/io/iohk/codecs/nio/test/utils",
    ],
)
