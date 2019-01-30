load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "test_interface_EXT",
    exports = [
        "//external:jar/org/scala_sbt/test_interface"
    ],
    neverlink = True,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

java_library(
    name = "test_interface",
    exports = [
        "//external:jar/org/scala_sbt/test_interface"
    ],
    neverlink = False,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


