package tadp.lenses.terraria

import monocle.macros.syntax.lens._
import monocle.macros.Lenses

trait Armazon
case object Hierro extends Armazon
case object Oro extends Armazon
case object Plata extends Armazon

trait Material
case object Cristal extends Material
case object Vidrio extends Material
case object Polimero extends Material

trait Decoracion
case object Rubi extends Decoracion
case object Zafiro extends Decoracion
case object Esmeralda extends Decoracion

case class Ropa(superior: String, inferior: String, calzado: String, sombrero: String)

case class Lentes(material: Material, graduacion: Double) {
  def setGraduacion(graduacion: Double) = this.copy(graduacion= graduacion)
}

case class Gafas(armazon: Armazon, lentes: Lentes, decoracion: Decoracion) {
  def setLentes(lente: Lentes) = this.copy(lentes= lente)
  def setGraduacionGafas(graduacion: Double) = this.copy(
    lentes= lentes.copy(
      graduacion= graduacion
    )
  )
}

case class Personaje(nombre: String, gafas: Gafas, ropa: Ropa)