package tadp.lenses.terraria

import monocle.macros.syntax.lens._
import monocle.macros.Lenses
import tadp.lenses.{Prism, Optional}

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
  def setLentes(lente: Lentes): Gafas = this.copy(lentes= lente)
  def setGraduacionGafas(graduacion: Double): Gafas = this.copy(
    lentes= lentes.copy(
      graduacion= graduacion
    )
  )
}

case class Personaje(nombre: String, gafas: Gafas, ropa: Ropa)

trait Item
case object Espada extends Item
case object Armadura extends Item

trait Cofre
case object CofreVacio extends Cofre
case class ContenidoCofre(item: Item) extends Cofre


object Test {
  val lentes = Lentes(Cristal, 2.0)
  val gafas = Gafas(Plata, lentes, Rubi)
  val ropaNormal = Ropa("Camisa", "Pantalon", "zapatillas", "")
  val personajePrisma = Prism[Personaje, String] {
    case Personaje(n, _, _) => Some(n)
    case _ => None
  }(nombre => Personaje(nombre, gafas, ropaNormal))


  val cofreConArmaduraOptional = Optional[Cofre, Item] {
    case cofre: ContenidoCofre => Some(cofre.item)
    case _ => None
  } { contenido => {
    case c: ContenidoCofre => c.copy(item = contenido)
    case cofre => cofre
  }
  }
}