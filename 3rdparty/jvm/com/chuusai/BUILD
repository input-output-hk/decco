load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "shapeless_EXT",
    jars = [
        "//external:jar/com/chuusai/shapeless_2_12"
    ],
    neverlink = 1,
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        "//3rdparty/jvm/org/typelevel:macro_compat"
    ],
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "shapeless",
    jars = [
        "//external:jar/com/chuusai/shapeless_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        "//3rdparty/jvm/org/typelevel:macro_compat"
    ],
    visibility = [
        "//visibility:public"
    ]
)


