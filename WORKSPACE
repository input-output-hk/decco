load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

rules_scala_version="d3329c48511fc98c51bf4e28af14d231493a40d0"
http_archive(
    name = "io_bazel_rules_scala",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip"%rules_scala_version,
    type = "zip",
    strip_prefix= "rules_scala-%s" % rules_scala_version,
    sha256 = "a60a64b117662807538cfe741840dc5d21c72849ef52c3d4580219183b04accb"
)

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
scala_repositories(("2.12.8", {
    "scala_compiler": "f34e9119f45abd41e85b9e121ba19dd9288b3b4af7f7047e86dc70236708d170",
    "scala_library": "321fb55685635c931eba4bc0d7668349da3f2c09aee2de93a70566066ff25c28",
    "scala_reflect": "4d6405395c4599ce04cea08ba082339e3e42135de9aae2923c9f5367e957315a"
}))

load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
scala_register_toolchains()

load("//3rdparty:workspace.bzl", "maven_dependencies")
maven_dependencies()


load("//bazel_tools:java.bzl", "java_home_runtime")
java_home_runtime(name = "java_home")

load("//bazel_tools:protobuf.bzl", "protobuf_dep")
protobuf_dep()