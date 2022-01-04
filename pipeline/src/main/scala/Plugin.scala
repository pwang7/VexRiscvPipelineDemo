package vexriscv.plugin

import vexriscv.{Pipeline, Stage}
import spinal.core.{Area, Nameable}

/** Created by PIC32F_USER on 03/03/2017.
  */
trait Plugin[Tp <: Pipeline] extends Nameable {
  var pipeline: Tp = null.asInstanceOf[Tp]
  setName(this.getClass.getSimpleName.replace("$", ""))

  // Used to setup things with other plugins
  def setup(pipeline: Tp): Unit = { identity(pipeline); () }

  //Used to flush out the required hardware (called after setup)
  def build(pipeline: Tp): Unit

  implicit class implicitsStage(stage: Stage) {
    def plug[T <: Area](area: T): T = {
      area.setCompositeName(stage, getName()).reflectNames(); area
    }
  }
  implicit class implicitsPipeline(stage: Pipeline) {
    def plug[T <: Area](area: T) = {
      area.setName(getName()).reflectNames(); area
    }
  }
}
