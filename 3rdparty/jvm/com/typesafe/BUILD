load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "config_EXT",
    exports = [
        "//external:jar/com/typesafe/config"
    ],
    neverlink = True,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

java_library(
    name = "config",
    exports = [
        "//external:jar/com/typesafe/config"
    ],
    neverlink = False,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


