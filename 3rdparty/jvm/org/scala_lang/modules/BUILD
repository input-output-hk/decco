load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "scala_java8_compat_EXT",
    jars = [
        "//external:jar/org/scala_lang/modules/scala_java8_compat_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "scala_java8_compat",
    jars = [
        "//external:jar/org/scala_lang/modules/scala_java8_compat_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "scala_xml_EXT",
    jars = [
        "//external:jar/org/scala_lang/modules/scala_xml_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "scala_xml",
    jars = [
        "//external:jar/org/scala_lang/modules/scala_xml_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    visibility = [
        "//visibility:public"
    ]
)


