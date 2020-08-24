package tadp.lenses

import org.scalatest._
import flatspec._
import matchers._
import terraria._


class LensTest extends AnyFlatSpec with should.Matchers {

  "With Lens" should "get and set a value properly" in  {
      val graduacionLens = Lens[Lentes, Double](
          get = _.graduacion,
          set = (o, v) => o.copy(graduacion = v)
        )

      val l = Lentes(Cristal, 2.0)
      graduacionLens.get(l) should be (2.0)
      graduacionLens.set(l, 3) should be (Lentes(Cristal, 3.0))
  }

  "When we compose two lenses it" should "return a valid value still" in {
    val graduacionLens = Lens[Lentes, Double](
      get = _.graduacion,
      set = (o, v) => o.copy(graduacion = v)
    )
    val lentesGafasLens = Lens[Gafas, Lentes](
      get = _.lentes,
      set = (o, v) => o.copy(lentes = v)
    )
    val graduacionGafasLens: Lens[Gafas, Double] = Lens.compose(lentesGafasLens, graduacionLens)

    val gafas = Gafas(Oro, Lentes(Cristal, 2.0), Rubi)

    graduacionGafasLens.get(gafas) should be (2.0)
    graduacionGafasLens.set(gafas, 4.3) should be (gafas.copy(lentes = Lentes(Cristal, 4.3)))
  }
}