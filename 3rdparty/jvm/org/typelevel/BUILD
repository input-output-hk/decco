load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "macro_compat_EXT",
    jars = [
        "//external:jar/org/typelevel/macro_compat_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "macro_compat",
    jars = [
        "//external:jar/org/typelevel/macro_compat_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    visibility = [
        "//visibility:public"
    ]
)


