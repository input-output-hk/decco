load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "test_interface_EXT",
    jars = [
        "//external:jar/org/scala_sbt/test_interface"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "test_interface",
    jars = [
        "//external:jar/org/scala_sbt/test_interface"
    ],
    visibility = [
        "//visibility:public"
    ]
)


