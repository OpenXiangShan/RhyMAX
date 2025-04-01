import mill._
import scalalib._
import scalafmt._
import $file.common
import $file.`rocket-chip`.common
import $file.`rocket-chip`.cde.common
import $file.`rocket-chip`.hardfloat.common

// import Mill dependency
import mill._
import mill.define.Sources
import mill.modules.Util
import mill.scalalib.scalafmt.ScalafmtModule
import mill.scalalib.TestModule.ScalaTest
import mill.scalalib._
// support BSP
import mill.bsp._

val defaultScalaVersion = "2.13.15"

def defaultVersions = Map(
  "chisel"        -> ivy"org.chipsalliance::chisel:6.6.0",
  "chisel-plugin" -> ivy"org.chipsalliance:::chisel-plugin:6.6.0",
  "chiseltest"    -> ivy"edu.berkeley.cs::chiseltest:6.0.0",
  "scalatest"     -> ivy"org.scalatest::scalatest:3.2.19"
)

trait HasChisel extends ScalaModule {
  def chiselModule: Option[ScalaModule] = None

  def chiselPluginJar: T[Option[PathRef]] = None

  def chiselIvy: Option[Dep] = Some(defaultVersions("chisel"))

  def chiselPluginIvy: Option[Dep] = Some(defaultVersions("chisel-plugin"))

  override def scalaVersion = defaultScalaVersion

  override def scalacOptions = super.scalacOptions() ++
    Agg("-language:reflectiveCalls", "-Ymacro-annotations", "-Ytasty-reader")

  override def ivyDeps = super.ivyDeps() ++ Agg(chiselIvy.get)

  override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(chiselPluginIvy.get)
}

object rocketchip extends `rocket-chip`.common.RocketChipModule with HasChisel {
  val rcPath = os.pwd / "rocket-chip"
  override def millSourcePath = rcPath

  def mainargsIvy = ivy"com.lihaoyi::mainargs:0.7.0"
  def json4sJacksonIvy = ivy"org.json4s::json4s-jackson:4.0.7"

  object macros extends `rocket-chip`.common.MacrosModule with HasChisel {
    def scalaReflectIvy = ivy"org.scala-lang:scala-reflect:${scalaVersion}"
  }

  object cde extends `rocket-chip`.cde.common.CDEModule with HasChisel {
    override def millSourcePath = rcPath / "cde" / "cde"
  }

  object hardfloat extends `rocket-chip`.hardfloat.common.HardfloatModule with HasChisel {
    override def millSourcePath = rcPath / "hardfloat" / "hardfloat"
  }

  def macrosModule = macros
  def hardfloatModule = hardfloat
  def cdeModule = cde
}

object utility extends SbtModule with HasChisel {
  override def millSourcePath = os.pwd / "Utility"
  override def moduleDeps = super.moduleDeps ++ Seq(rocketchip)
}

object AME extends SbtModule with HasChisel with millbuild.common.AMEModule with ScalaModule with ScalafmtModule {  m =>
  override def millSourcePath = millOuterCtx.millSourcePath
  
  // 显式设置主源代码路径（虽然默认应该能自动发现）
  override def sources = T.sources {
    super.sources() ++ Seq(PathRef(millSourcePath / "src" / "main" / "scala"))
  }

  def rocketModule: ScalaModule = rocketchip
  def utilityModule: ScalaModule = utility

  //override def scalacOptions = super.scalacOptions() ++ Agg("-deprecation", "-feature")

  override def scalaVersion = "2.13.15"

  override def scalacOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit"
  )

  override def ivyDeps             = Agg(ivy"org.chipsalliance::chisel:6.6.0")
  override def scalacPluginIvyDeps = Agg(ivy"org.chipsalliance:::chisel-plugin:6.6.0")

  object test extends ScalaTests with TestModule.ScalaTest with ScalafmtModule {
    override def ivyDeps = m.ivyDeps() ++ Agg(
      ivy"org.scalatest::scalatest::3.2.19",
      // for formal flow in future
      ivy"edu.berkeley.cs::chiseltest:6.0.0"
    )
  }

  
}