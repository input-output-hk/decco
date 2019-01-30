load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "scala_java8_compat_EXT",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    jars = [
        "//external:jar/org/scala_lang/modules/scala_java8_compat_2_12"
    ],
    neverlink = True,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

scala_import(
    name = "scala_java8_compat",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    jars = [
        "//external:jar/org/scala_lang/modules/scala_java8_compat_2_12"
    ],
    neverlink = False,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)



scala_library(
    name = "scala_parser_combinators_EXT",
    exports = [
        "@io_bazel_rules_scala_scala_parser_combinators//:io_bazel_rules_scala_scala_parser_combinators"
    ],
    neverlink = True,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

scala_library(
    name = "scala_parser_combinators",
    exports = [
        "@io_bazel_rules_scala_scala_parser_combinators//:io_bazel_rules_scala_scala_parser_combinators"
    ],
    neverlink = False,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)



scala_library(
    name = "scala_xml_EXT",
    exports = [
        "@io_bazel_rules_scala_scala_xml//:io_bazel_rules_scala_scala_xml"
    ],
    neverlink = True,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

scala_library(
    name = "scala_xml",
    exports = [
        "@io_bazel_rules_scala_scala_xml//:io_bazel_rules_scala_scala_xml"
    ],
    neverlink = False,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


