load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "objenesis_EXT",
    exports = [
        "//external:jar/org/objenesis/objenesis"
    ],
    neverlink = True,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

java_library(
    name = "objenesis",
    exports = [
        "//external:jar/org/objenesis/objenesis"
    ],
    neverlink = False,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


