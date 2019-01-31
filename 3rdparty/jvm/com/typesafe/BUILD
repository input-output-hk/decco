load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "config_EXT",
    jars = [
        "//external:jar/com/typesafe/config"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "config",
    jars = [
        "//external:jar/com/typesafe/config"
    ],
    visibility = [
        "//visibility:public"
    ]
)


