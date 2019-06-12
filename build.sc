// build.sc

import mill._, scalalib._, os._
import mill.api.Loose.Agg

trait CompositeModule extends ScalaModule { outer =>

  def sources = {
    T.sources { millSourcePath }
  }

  def allSourceFiles = T {
    val submodules =
      millModuleDirectChildren.map(_.millSourcePath)
    def isHiddenFile(path: os.Path) = path.last.startsWith(".")
    def isFromSubmodule(path: os.Path) = {
      submodules.exists { path.startsWith _ }
    }
    for {
      root <- allSources()
      if os.exists(root.path)
      path <- (if (os.isDir(root.path)) os.walk(root.path) else Seq(root.path))
      if os
        .isFile(path) && ((path.ext == "scala" || path.ext == "java") && !isHiddenFile(path) && !isFromSubmodule(path))
    } yield PathRef(path)
  }

  trait Tests extends super.Tests with CompositeModule {
    def ivyDepsExtra: Agg[Dep] = Agg()
    final def ivyDeps = ivyDepsExtra ++ testingLibrary
    def moduleDepsExtra: Seq[JavaModule] = Seq()
    final def moduleDeps = (Seq(outer) ++ moduleDepsExtra)
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

trait IOHKModule extends CompositeModule {

  def scalaVersion = "2.12.7"

  trait IOHKTest extends Tests {
    def testingLibrary =
      deps.scalatest
  }
}

object src extends Module {
  object io extends Module {
    object iohk extends Module {

      object decco extends IOHKModule {

        object auto extends IOHKModule {

          def ivyDeps =
            deps.shapeless ++
              deps.akkaActor

          def moduleDeps = Seq(decco)
        }

        object test extends IOHKTest {
          def moduleDepsExtra = Seq(auto, utils)
          def ivyDepsExtra = deps.scalacheck

          object utils extends IOHKModule {
            def ivyDeps =
              deps.scalacheck ++
                deps.scalatest

            def moduleDeps = Seq(decco)
          }
        }
      }

    }
  }
}
