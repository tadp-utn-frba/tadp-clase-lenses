import monocle.macros.syntax.lens._
import tadp.lenses.terraria._


val readingLens: Lentes = Lentes(Cristal, 2.0)

val adjustedLens = Lentes.graduacion.modify(_ + 1.1)(readingLens)

val gafasViejo = Gafas(Plata, readingLens, Rubi)
val cloud = Personaje("Cloud", gafasViejo, Ropa("", "", "", ""))



(Personaje.gafas composeLens Gafas.lentes).get(cloud)

(Personaje.gafas composeLens Gafas.lentes composeLens Lentes.graduacion).modify(_ + 1.1)(cloud)

cloud.lens(_.gafas.lentes.graduacion).modify(_ + 1.1)