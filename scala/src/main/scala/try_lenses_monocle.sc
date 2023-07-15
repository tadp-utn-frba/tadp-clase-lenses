import monocle.macros.syntax.lens._
import tadp.lenses.terraria._
import monocle.Lens
import monocle.macros.GenLens

//val gafas = Lens[Personaje, Gafas](_.gafas)(g => p => p.copy(gafas = g))
val gafas: Lens[Personaje, Gafas] = GenLens[Personaje] (_.gafas)

object TerrariaLenses {
  val gafas: Lens[Personaje, Gafas] = GenLens[Personaje] (_.gafas)
  val lentes: Lens[Gafas, Lentes] = GenLens[Gafas] (_.lentes)
  val graduacion: Lens[Lentes, Double] = GenLens[Lentes] (_.graduacion)
  val material: Lens[Lentes, Material] = GenLens[Lentes] (_.material)
}

val readingLens: Lentes = Lentes(Cristal, 2.0)
val gafasViejo = Gafas(Plata, readingLens, Rubi)
val cloud = Personaje("Cloud", gafasViejo, Ropa("", "", "", ""))

val adjustedLens = TerrariaLenses.graduacion.modify(_ + 1.1)(readingLens) // Lentes(Cristal, 3.1)


(gafas composeLens TerrariaLenses.lentes).get(cloud)

(gafas composeLens TerrariaLenses.lentes composeLens TerrariaLenses.graduacion).get(cloud)


(gafas composeLens TerrariaLenses.lentes composeLens TerrariaLenses.graduacion).modify(_ + 1.1)(cloud)