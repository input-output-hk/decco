{ system ? builtins.currentSystem
, crossSystem ? null
, config ? {}
, nixpkgs ? import ./nix/nixpkgs.nix
, pkgs ? import nixpkgs {inherit system crossSystem config;}
}:
with pkgs; {

  decco = buildBazelPackage rec {
    name = "decco";

    meta = with stdenv.lib; {
      homepage = "https://github.com/input-output-hk/decco";
      description = "A codec library";
      license = licenses.apsl20;
      platforms = platforms.all;
    };

    src = nix-gitignore.gitignoreSource [] ./.;

    bazelTarget = "//src/io/iohk/codecs";

    fetchAttrs.sha256 = "1422qh3m2r2vmshaf3x1hk06ajqdbv3mmw4sdf5w7c2qhpghxf4m";

    buildAttrs = {
      postPatch = ''
        # Configure Bazel to use JDK8
        cat >> .bazelrc <<EOF
        build --host_java_toolchain=@bazel_tools//tools/jdk:toolchain_hostjdk8
        build --java_toolchain=@bazel_tools//tools/jdk:toolchain_hostjdk8
        build --host_javabase=@local_jdk//:jdk
        build --javabase=@local_jdk//:jdk
        EOF
      '';
      preConfigure = ''
        export JAVA_HOME="${jre.home}"
      '';
      installPhase = ''
        mkdir -p $out/bin/bazel-bin/src/io/iohk/codecs

        cp bazel-bin/src/io/iohk/codecs/codecs.jar $out/bin/bazel-bin/src/io/iohk/codecs
      '';
    };
  };
}