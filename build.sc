// build.sc

import mill._, scalalib._, os._
import mill.api.Loose.Agg
import mill.scalalib.publish._
trait CompositeModule extends ScalaModule { outer =>

  override def sources = {
    T.sources { millSourcePath }
  }

  override def allSourceFiles = T {
    val submodules =
      millModuleDirectChildren.map(_.millSourcePath)
    def isHiddenFile(path: os.Path) = path.last.startsWith(".")
    def isFromSubmodule(path: os.Path) = {
      submodules.exists { path.startsWith _ }
    }
    for {
      root <- allSources()
      if os.exists(root.path)
      path <- if (os.isDir(root.path)) os.walk(root.path) else Seq(root.path)
      if os
        .isFile(path) && ((path.ext == "scala" || path.ext == "java") && !isHiddenFile(path) && !isFromSubmodule(path))
    } yield PathRef(path)
  }

  trait Tests extends super.Tests with CompositeModule {
    def ivyDepsExtra: Agg[Dep] = Agg()
    override final def ivyDeps = ivyDepsExtra ++ testingLibrary
    def moduleDepsExtra: Seq[PublishModule] = Seq()
    override final def moduleDeps = (Seq(outer) ++ moduleDepsExtra)
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def testingLibrary = Agg(ivy"org.scalatest::scalatest:3.0.5")

  }

}

object deps {
  val shapeless =
    Agg(ivy"com.chuusai::shapeless:2.3.3")
  val akkaActor =
    Agg(ivy"com.typesafe.akka::akka-actor:2.5.12")
  val akkaTestkit =
    Agg(ivy"com.typesafe.akka::akka-testkit:2.5.12")
  val scalacheck =
    Agg(ivy"org.scalacheck::scalacheck:1.14.0")
  val scalatest =
    Agg(ivy"org.scalatest::scalatest:3.0.5")
}

trait IOHKModule extends CompositeModule with PublishModule {

  def scalaVersion = "2.12.7"

  def publishVersion = "1.0-SNAPSHOT"

  def pomSettings = PomSettings(
    description = "codec library",
    organization = "io.iohk",
    url = "https://github.com/input-output-hk/decco",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("input-output-hk", "decco"),
    developers = Seq()
  )

  trait IOHKTest extends Tests {
    override def testingLibrary =
      deps.scalatest
  }
}

object src extends Module {
  object io extends Module {
    object iohk extends Module {

      object decco extends IOHKModule {
        override def artifactName = "decco"

        object auto extends IOHKModule {
          override def artifactName = "decco-auto"

          override def ivyDeps =
            deps.shapeless ++
              deps.akkaActor

          override def moduleDeps = Seq(decco)
        }

        object test extends IOHKTest {

          override def moduleDepsExtra = Seq(auto, utils)
          override def ivyDepsExtra = deps.scalacheck

          object utils extends IOHKModule {
            override def artifactName = "decco-test-utils"

            override def ivyDeps =
              deps.scalacheck ++
                deps.scalatest

            override def moduleDeps = Seq(decco)
          }

          def testOne(args: String*) = T.command {
            super.runMain("org.scalatest.run", args: _*)
          }
        }
      }

    }
  }
}
