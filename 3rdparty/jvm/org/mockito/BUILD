load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "mockito_core_EXT",
    jars = [
        "//external:jar/org/mockito/mockito_core"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "mockito_core",
    jars = [
        "//external:jar/org/mockito/mockito_core"
    ],
    runtime_deps = [
        "//3rdparty/jvm/net/bytebuddy:byte_buddy",
        "//3rdparty/jvm/net/bytebuddy:byte_buddy_agent",
        "//3rdparty/jvm/org/objenesis:objenesis"
    ],
    visibility = [
        "//visibility:public"
    ]
)


