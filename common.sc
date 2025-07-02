import mill._
import scalalib._

trait AMEModule extends ScalaModule {

  def rocketModule: ScalaModule

  def utilityModule: ScalaModule

  //def fpuModule: ScalaModule

  //override def moduleDeps = super.moduleDeps ++ Seq(rocketModule, utilityModule, fpuModule)
  override def moduleDeps = super.moduleDeps ++ Seq(rocketModule, utilityModule)

}
