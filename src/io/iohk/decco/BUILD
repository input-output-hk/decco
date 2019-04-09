load("//scala:rules.bzl", "scala_library", "scala_test")

scala_library(
    name = "decco",
)

scala_test(
    name = "tests",
    external = [
        "org.mockito:mockito-core",
        "org.scalacheck:scalacheck%",
    ],
    deps = [
        "decco",
        "//src/io/iohk/decco/auto",
        "//src/io/iohk/decco/test/utils",
    ],
)
