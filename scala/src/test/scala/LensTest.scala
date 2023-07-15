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

  "When checking the identity lens laws"  should "be true" in {
    val graduacionLens = Lens[Lentes, Double](
      get = _.graduacion,
      set = (o, v) => o.copy(graduacion = v)
    )

    val l = Lentes(Cristal, 2.0)
    Lens.identity(graduacionLens, l) should be(true)
  }

  "When checking the retention lens laws" should "be true" in {
    val graduacionLens = Lens[Lentes, Double](
      get = _.graduacion,
      set = (o, v) => o.copy(graduacion = v)
    )

    val l = Lentes(Cristal, 2.0)
    Lens.retention(graduacionLens, l, 3.0) should be(true)
  }

  "When checking the putPut lens laws" should "be true" in {
    val graduacionLens = Lens[Lentes, Double](
      get = _.graduacion,
      set = (o, v) => o.copy(graduacion = v)
    )

    val l = Lentes(Cristal, 2.0)
    Lens.doubleSet(graduacionLens, l, 3.0, 3.0) should be(true)
  }

  "when using a personaje prisma" should "work..." in {
    val lentes = Lentes(Cristal, 2.0)
    val gafas = Gafas(Plata, lentes, Rubi)
    val ropaNormal = Ropa("Camisa", "Pantalon", "zapatillas", "")
    val personajePrisma = Prism[Personaje, String] {
      case Personaje(n, _, _) => Some(n)
      case _ => None
    }(nombre => Personaje(nombre, gafas, ropaNormal))

    personajePrisma.getOption(Personaje("cloud", gafas, ropaNormal)) should be(Some("cloud"))
    personajePrisma.reverseGet("cloud") should be(Personaje("cloud", gafas, ropaNormal))
  }

  "when using an optional of a chest" should "work as well..." in {
    val cofreConArmaduraOptional = Optional[Cofre, Item] {
      case cofre: ContenidoCofre => Some(cofre.item)
      case _ => None
    } { contenido => {
      case c: ContenidoCofre => c.copy(item = contenido)
      case cofre => cofre
    }
    }

    cofreConArmaduraOptional.getOption(ContenidoCofre(Espada)) should be(Some(Espada))

    val f = cofreConArmaduraOptional.set(Espada)
    f(ContenidoCofre(Armadura)) should be(ContenidoCofre(Espada))
    f(CofreVacio) should be(CofreVacio)
  }
}