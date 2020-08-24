# Lenses en Haskell y Scala

## Inmutabilidad

Uno de los rasgos más distintivos de la programación funcional es la inmutabilidad, aunque bien existen muchos lenguajes funcionales mutables (Clojure por ejemplo). La inmutabilidad está buena por varias razones: 

- Código más fácil sobre el cual razonar
- Como no tengo estado compartido, no tengo problemas de concurrencia
- Permite realizar optimizaciones reutilizando operaciones y otras más.

Pero esta inmutabilidad es a su vez otro problema: los datos no cambian. Como los datos no cambian, para emular el efecto, tenemos que construir datos nuevos a partir de los que tenemos. Y esto puede rápidamente volverse un poco tedioso si tenemos que cambiar un valor en una estructura anidada (y ni hablar si esa estructura está en una lista).

### Un caso práctico

Estamos en el mundo de terraria! Tenemos personajes, que tienen un nombre y obviamente llevan ropa. La ropa está conformada por la prenda superior, la prenda inferior, el calzado y un sombrero. Además, los personajes son re cancheros y usan gafas, ya sea unos lentes de sol para tirar facha o unos anteojos con aumento para ver mejor.
Las gafas tienen un armazón de oro, plata o hierro; un par de lentes y una decoración que puede ser de rubí, zafiro o esmeralda (quién te conoce pokemon). Los lentes a su vez se componen por su graduación y por tener (un número positivo) el material con el que están hecho: vidrio, cristal o polímero.
Queremos cambiar la graduación de los lentes de un personaje.

```haskell
data Personaje = Personaje {
  nombre :: String,
  gafas :: Gafas,
  ropa :: Ropa
}

data Ropa = Ropa {
  superior :: String,
  inferior :: String,
  calzado :: String,
  sombrero :: String
}

data Gafas = Gafas {
  armazon :: Armazon,
  lentes :: Lentes,
  decoracion :: Decoracion
}

data Lentes = Lente {
  material :: Material,
  graduacion :: Double
}

data Armazon = Hierro | Oro | Plata
data Material = Cristal | Vidrio | Polimero
data Decoracion = Rubi | Zafiro | Esmeralda
```

Tenemos que ir de lo más específico a lo más general: empezamos generando un setter inmutable para la graduación de los lentes, luego un setter para los lentes de las gafas y luego un setter de gafas para los personajes:

```haskell
setGraduacion :: Double -> Lentes -> Lentes
setGraduacion graduacion l = l { graduacion = graduacion }

setLentes :: Lentes -> Gafas -> Gafas
setLentes lentes g = g { lentes = lentes }

setGafas :: Gafas -> Personaje -> Personaje
setGafas gafas p = p { gafas = gafas }

setGraduacionGafas :: Double -> Gafas -> Gafas
setGraduacionGafas graduacion g = g { lentes = setGraduacion graduacion . lentes $ g }

cambiarGraduacion :: Double -> Personaje -> Personaje
cambiarGraduacion graduacion p = p { gafas = setGraduacionGafas graduacion . gafas $ p }
```

Y… es bastante feo. Tuvimos que definir 5 funciones, las dos últimas con nombres bastantes verbosos y encima son un poco complejas de seguir. Y por cualquier otro campo que queramos actualizar, tenemos que hacer lo mismo.
Y para agregar sal en la herida: si no se dieron cuenta, estamos repitiendo lógica. en `setGraduacionPersonaje` y `setGraduacionGafas` estamos haciendo algo muy similar en ambas:

- Reciben el valor anidado a actualizar.
- Reciben la estructura “base”.
- Acceden un valor de la estructura.
- Modifican este valor extraído.
- Guardan el valor actualizado en la estructura original.

Entonces, con un poco de sopa, decidimos abstraer esta lógica repetida en una función:

```haskell
type Setter a b = (b -> b) -> a -> a

setter :: (a -> b) -> (a -> b -> a) -> Setter a b
setter accessor modify update a = modify a . update . accessor $ a
```

Uff. Bueno, eso es duro de leer. Expliquemos por partes:
- accessor es la función que extrae el estado anidado de la estructura.
- update es la función que, sobre el valor que retorna modify, lo aplica sobre la estructura inicial.
- modify es la función que actualiza el valor extraído.
- a es la estructura original.

Definamos `cambiarGraduacion` en base a esta nueva función:

```haskell
setGraduacion :: Setter Lentes Double
setGraduacion = 
  setter graduacion (\lentes graduacion -> lentes { graduacion = graduacion })


setLentes :: Setter Gafas Lentes
setLentes = setter lentes (\gafas lentes -> gafas { lentes = lentes })

setGafas :: Setter Personaje Gafas
setGafas = setter gafas (\personaje gafas -> personaje { gafas = gafas })

setGraduacionPersonaje :: Setter Personaje Double
setGraduacionPersonaje = setGafas . setLentes . setGraduacion

cambiarGraduacion :: Double -> Personaje -> Personaje
cambiarGraduacion nuevaGraduacion = setGraduacionPersonaje (const nuevaGraduacion)
```

donde 

```haskell
const _ b = b
```

Tenemos definidos los tres setters en cada nivel de la estructura. Para definir un setter desde una capa más arriba, podemos componer ambos setters. Reemplacemos las firmas por el type original de Setter:

```haskell
setGraduacion :: (Double -> Double) -> Lentes -> Lentes
setLentes :: (Lentes -> Lentes) -> Gafas -> Gafas
setGafas :: (Gafas -> Gafas) -> Personaje -> Personaje
```

El primer parámetro que recibe cambiarGraduacion es una función (Double -> Double), que nos lo provee const (nuevoValor). Y por currificación, las firmas anteriores las podemos reescribir de la siguiente manera:

```haskell
setGraduacion :: (Double -> Double) -> (Lentes -> Lentes)
setLentes :: (Lentes -> Lentes) -> (Gafas -> Gafas)
setGafas :: (Gafas -> Gafas) -> (Personaje -> Personaje)
```

Ahora es mucho más evidente por qué puedo componer estas funciones.

Y podríamos terminar acá, regodearnos en la gloria de semejante descubrimiento, pero no inventamos nada, porque este concepto ya existe, conocido como *lenses*.

## Lenses

No inventamos nada, este concepto es el de lenses. Existen varias bibliotecas de optics varios, pero nos vamos a centrar en la original: [lens](https://hackage.haskell.org/package/lens). El principal problema que solucionan las lenses es esto que acabamos de ver: setters anidados. En su mínima expresión, los lenses son getters y setters en funcional. El tipo de Lens se define de la siguiente manera:

```haskell
type Lens a b = Monad m => (b -> m b) -> (a -> m a) 
```

Este es un tipo simplificado, no es la definición original, no es Haskell válido

Esto no es para nada trivial, y tampoco pretendemos que puedan escribir la definición de la función (ah, pero podríamos hacer un desafío de café con leche para esto). Es parecida a la firma de setter que armamos más arriba, pero es más genérica que nuestra definición. Para lo que nosotros queremos, podemos quedarnos con los mismos tipos que los que usamos para Setter:

```haskell
type Lens a b = (b -> b) -> a -> a
lens :: (a -> b) -> (a -> b -> a) -> (b -> b) -> a -> a --ey, esto es una Lens!
lens :: (a -> b) -> (a -> b -> a) -> Lens a b
```

### Setters

Por última vez, definamos `cambiarGraduacion` con las funciones de la biblioteca:

```haskell
cambiarGraduacion :: Double -> Personaje -> Personaje
cambiarGraduacion nuevaGraduacion = 
  over (gafas . lentes . graduacion) (const nuevaGraduacion)

over :: Lens a b -> (b -> b) -> a -> a
over unaLente f a = unaLente f a
```

over recibe una Lens a b, un mapeo (b -> b) y una estructura a, y aplica el mapeo sobre el valor b dentro de a. De hecho, este mapeo de pisar el valor original por uno nuevo es muy común, por lo que hay una función ya definida que hace esto mismo: `set`

```haskell
set :: Lens a b -> b -> a -> a

cambiarGraduacion :: Double -> Personaje -> Personaje
cambiarGraduacion = set (gafas . lentes . graduacion)
```

### Getters

Si bien en Haskell no tenemos el mismo problema con los getters, es un poco anti-intuitivo, teniendo que leer de derecha a izquierda el orden de anidamiento. Por ejemplo, para obtener la graduación de un personaje, deberíamos escribirlo así:

```haskell
graduacionPersonaje :: Personaje -> Double
graduacionPersonaje = graduacion . lentes . gafas
```

Y bien podríamos acostumbrarnos, quedarnos con esto and call it a day. Pero lenses no hace eso. Como tenemos los lenses autogenerados, y se componen en el orden inverso, para obtener un valor de una Lens, existe la función view que hace esto mismo:

```haskell
view :: Lens a b -> a -> b

graduacionPersonaje :: Personaje -> Double
graduacionPersonaje = view (gafas . lentes . graduacion)
```

## Lenses en Scala

Bien, volvamos un poco al mismo caso del ejemplo anterior de Terraria, solamente que ahora queremos migrarlo a Scala. veamos como quedan alguna de las funciones de setear las lentes o las graduaciones de las gafas.

```scala
case class Personaje(nombre: String, gafas: Gafas, ropa: Ropa)
case class Ropa(superior: String, inferior: String, calzado: String, sombrero: String)

case class Gafas(armazon: Armazon, lentes: Lentes, decoracion: Decoracion) {
  def setLentes(lente: Lentes): Gafas = this.copy(lentes= lente)
  
  def setGraduacionGafas(graduacion: Double) = this.copy(
    lentes= lentes.copy(
      graduacion= graduacion
    )
  )
}

case class Lentes(material: Material, graduacion: Double) {
  def setGraduacion(graduacion: Double): Lentes = this.copy(graduacion= graduacion)
}

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
```

`setLentes` y `setGraduacion` por ejemplo no parecen nada del otro mundo solamente un copy que nos devolverá una nueva instancia de lentes o de gafas. 

Pero veamos que si empezamos a tener que definir funciones que cambien la graduación pero de las gafas, como por ejemplo 

```scala
def setGraduacionDeLasGafas(gafas: Gafas,graduacion: Double): Gafas = gafas.copy(
    lentes= gafas.lentes.copy(
      graduacion= graduacion
    )
  )
```

vemos que no es tan problemático, pero si empezamos a tener mayores niveles de anidamiento, como el de la siguiente función

```scala
def setGraduacionDeLasGafasDeUnPersonaje(personaje: Personaje,graduacion: Double) = personaje.copy(
    gafas= personaje.gafas.copy(
      lentes= personaje.gafas.lentes.copy(
        graduacion= graduacion
      )
    )
  )
```

empezamos a ver que se empieza a perder un poco la expresividad y la claridad de la sintaxis.

### Lentes

Podemos introducir una primera intuición de lentes de la siguiente manera cumpliendo siempre que

> Una lente es una referencia de primera clase a una subparte de ~~un data type~~ una case class

Veamos de definir un poco a lo que necesitamos para definir mínimamente a un lens

```scala
case class Lens[O, V](
  get: O => V,
  set: (O, V) => O
)
```

ok, esto parecería la mínima expresión a la que podríamos llegar, entonces veamos de aplicarlo sobre nuestras estructuras para cambiar u obtener valores

De tener Lentes como

```scala
case class Lentes(material: Material, graduacion: Double) {
  def setGraduacion(graduacion: Double): Lentes = this.copy(graduacion= graduacion)
}
```
vamos a crear Lenses para la graduación de un 

```scala
trait TerrariaLenses {
  protected val graduacionLens = Lens[Lentes, Double](
    get = _.graduacion,
    set = (o, v) => o.copy(graduacion = v)
  )
  
  protected val lentesGafasLens = Lens[Gafas, Lentes](
    get = _.lentes,
    set = (o, v) => o.copy(lentes = v)
  )}
```
ahora al utilizarlo:

```scala
 val l = Lentes(Cristal, 2.0)
 val graduacion = graduacionLens.get(l)
 val new_l: Lentes= graduacionLens.set(l, 3)
```

vemos que tenemos nuestro primer lens, no parece nada novedoso o superador a lo que teniamos con las case clases. Veamos el ejemplo de ajustar la graduación de las lentes de una gafa

```scala
  protected val graduacionGafasLens = Lens[Gafas, Double](
    get = _.lentes.graduacion,
    set = (o, v) => o.copy(lentes = o.lentes.copy(graduacion= v))
  )
```

D’oh!... Otra vez el tema de los copy que empiezo a tener anidados… pero a no desesperarse, podemos tratar de usar algo como composición entre lenses y tratar de generalizarlo. Veamos..

```scala
object Lens {
  def compose[Outer, Inner, Value](
outer: Lens[Outer, Inner],
inner: Lens[Inner, Value]) = Lens[Outer, Value](
    get = outer.get andThen inner.get,
    set = (obj, value) => outer.set(obj, inner.set(outer.get(obj), value))
  )
}
```
Bien una vez que tenemos la composicion podemos reutilizar las otras dos lenses que teniamos antes en realidad, `graduacionLens` y `lentesGafasLens`

```scala
  protected val graduacionGafasLens: Lens[Gafas, Double] =
    Lens.compose(lentesGafasLens, graduacionLens)
```

Genial! ahora podemos componer medianamente facil y podemos imaginar a estos lenses como una especie de instancia de una funcion.

Entonces podemos decir que de alguna manera..

> Lens[ A , B ]  ~  A => B  (aclaracion: no es una afirmación 100% exacta)

y si tenemos otro lens

> Lens[ B , C ]  ~  B => C 

si estos se componen tenemos algo como 

>Lens[A, C] ~ A=>C.

Veamos que existen otras leyes que se cumplen además de la que vimos de transitividad.

### Propiedades de los Lenses

#### Identidad

Si hacemos un get y después seteamos el valor del get, el objeto que da igual (si lo se.. algo obvio)

```scala
  def identity[S, A](lens: Lens[S, A], s: S): Boolean =
    lens.set(s, lens.get(s)) == s
```

#### Retención

Esto es un poco el caso contrario, si a un tipo S le seteamos el valor a, y después sobre esto hacemos un get debería retener el valor y devolvernos a

```scala
 def retention[S, A](lens: Lens[S, A], s: S, a: A): Boolean =
    lens.get(lens.set(s, a)) == a
```

#### Doble set

si se setean dos veces un valor y después se hace un get, se obtiene el valor anterior

```scala
  def doubleSet[S, A](lens: Lens[S, A], s: S, a: A, b: A): Boolean =
    lens.get(lens.set(lens.set(s, a), b)) == b
```

En suma nuestra mini implementación (algo naive) de lenses en Scala queda como

```scala
case class Lens[O, V](
                       get: O => V,
                       set: (O, V) => O
                     )
object Lens {
  def compose[Outer, Inner, Value](
            outer: Lens[Outer, Inner],
            inner: Lens[Inner, Value]
  ) = Lens[Outer, Value](
    get = outer.get andThen inner.get,
    set = (obj, value) => outer.set(obj, inner.set(outer.get(obj), value))
  )

  def identity[S, A](lens: Lens[S, A], s: S): Boolean =
    lens.set(s, lens.get(s)) == s

  def retention[S, A](lens: Lens[S, A], s: S, a: A): Boolean =
    lens.get(lens.set(s, a)) == a

  def doubleSet[S, A](lens: Lens[S, A], s: S, a: A, b: A): Boolean =
    lens.get(lens.set(lens.set(s, a), b)) == b
}
```

Los lenses son estructuras referenciales del paradigma funcional, pero no son la única estructura, existen una estructura generalizada de estos que se llaman optics. Y ahora estaremos utilizando una librería que ya los implementa de manera más seria llamada Monocle


### Monocle en Scala

Tal como lo define la librería monocle, los optics son abstracciones y estructuras que nos permiten trabajar con objetos inmutables:

> Optics are a group of purely functional abstractions to manipulate (get, set, modify, …) immutable objects.

Con monocle, podemos tambien definir lenses de manera bastante similar a nuestra intuicion

```scala
  import monocle.Lens
  val gafas = Lens[Personaje, Gafas](_.gafas)(g => p => p.copy(gafas = g))
```

Aunque tambien podemos utilizar una macro *GenLens* para no tener que repetir a cada rato codigo que es bastante similar por cada lens que tenemos:

```scala
import monocle.Lens
import monocle.macros.GenLens
val gafas: Lens[Personaje, Gafas] = GenLens[Personaje] (_.gafas)
```
con lo cual podemos definir los lenses de una manera bastante simple ahora….

```scala
object TerrariaLenses {
  val gafas: Lens[Personaje, Gafas] = GenLens[Personaje] (_.gafas)
  val lentes: Lens[Gafas, Lentes] = GenLens[Gafas] (_.lentes)
  val graduacion: Lens[Lentes, Double] = GenLens[Lentes] (_.graduacion)
  val material: Lens[Lentes, Material] = GenLens[Lentes] (_.material)
}
```

Otra cosa que podemos agregar ahora a los lenses es el de primero hacer get y después set con modify

```scala
val readingLens: Lentes = Lentes(Cristal, 2.0)

val adjustedLens = graduacion.modify(_ + 1.1)(readingLens) # Lentes(Cristal, 3.1)
```

con lo cual ahora podemos incluso usar el composeLens si queremos acceder de manera anidada a las estructuras internas de un personaje 

```scala
val readingLens: Lentes = Lentes(Cristal, 2.0)
val gafasViejo = Gafas(Plata, readingLens, Rubi)
val cloud = Personaje("Cloud", gafasViejo, Ropa("", "", "", ""))

(gafas composeLens lentes).get(cloud) 

(gafas composeLens lentes composeLens graduacion).get(cloud)
```

y si quiero modificar a los lentes de nuestro personaje y devolverlo podemos hacer algo como:

```scala
val cloud = Personaje("Cloud", gafasViejo, Ropa("", "", "", ""))
  => Personaje(Cloud,Gafas(Plata,Lentes(Cristal,2.0),Rubi),Ropa(,,,))

(gafas composeLens lentes composeLens graduacion).modify(_ + 1.1)(cloud) => Personaje(Cloud,Gafas(Plata,Lentes(Cristal,3.1),Rubi),Ropa(,,,))
```

incluso se puede mejorar esto con la sintaxis que tiene monocle de lens

```scala
import monocle.macros.syntax.lens._

cloud.lens(_.gafas.lentes.graduacion).modify(_ + 1.1)  => Personaje(Cloud,Gafas(Plata,Lentes(Cristal,3.1),Rubi),Ropa(,,,))
```

Para la generacion incluso podemos ir un poco mas alla de lo que teniamos con GenLens… ahora podemos agregar la annotation `@Lenses` y nos generara los lenses para cada uno de los atributos de clase, entonces nuestro ejemplo seria ahora:

```scala
@Lenses case class Lentes(material: Material, graduacion: Double) {
  def setGraduacion(graduacion: Double) = this.copy(graduacion= graduacion)
}

@Lenses case class Gafas(armazon: Armazon, lentes: Lentes, decoracion: Decoracion) {
  def setLentes(lente: Lentes) = this.copy(lentes= lente)
  def setGraduacionGafas(graduacion: Double) = this.copy(
    lentes= lentes.copy(
      graduacion= graduacion
    )
  )
}

@Lenses case class Personaje(nombre: String, gafas: Gafas, ropa: Ropa)
```

y ahora nisiquiera necesitaremos crear nosotros los lenses:

```scala
val readingLens: Lentes = Lentes(Cristal, 2.0)
val adjustedLens = Lentes.graduacion.modify(_ + 1.1)(readingLens)

(Personaje.gafas composeLens Gafas.lentes).get(cloud) => Lentes(Cristal,2.0)

(Personaje.gafas composeLens Gafas.lentes composeLens Lentes.graduacion).modify(_ + 1.1)(cloud) => Personaje(Cloud,Gafas(Plata,Lentes(Cristal,3.1),Rubi),Ropa(,,,))
```

Con lo cual nuestro ejemplo de terraria quedaria finalmente como:

```scala
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

@Lenses case class Lentes(material: Material, graduacion: Double) {
  def setGraduacion(graduacion: Double) = this.copy(graduacion= graduacion)
}

@Lenses case class Gafas(armazon: Armazon, lentes: Lentes, decoracion: Decoracion) {
  def setLentes(lente: Lentes) = this.copy(lentes= lente)
  def setGraduacionGafas(graduacion: Double) = this.copy(
    lentes= lentes.copy(
      graduacion= graduacion
    )
  )
}

@Lenses case class Personaje(nombre: String, gafas: Gafas, ropa: Ropa)
```

También monocle cumple con varias de las leyes de lenses que vimos antes y mas (shamelessly taken from https://github.com/optics-dev/Monocle/blob/385085a24ec2561d0892a99ef37a51ba2ea43402/core/shared/src/main/scala/monocle/law/LensLaws.scala)

```scala
import monocle.Lens
import monocle.internal.IsEq

import cats.data.Const
import cats.Id

case class LensLaws[S, A](lens: Lens[S, A]) {
  import IsEq.syntax

  def getSet(s: S): IsEq[S] =
    lens.set(lens.get(s))(s) <==> s

  def setGet(s: S, a: A): IsEq[A] =
    lens.get(lens.set(a)(s)) <==> a

  def setIdempotent(s: S, a: A): IsEq[S] =
    lens.set(a)(lens.set(a)(s)) <==> lens.set(a)(s)

  def modifyIdentity(s: S): IsEq[S] =
    lens.modify(identity)(s) <==> s

  def composeModify(s: S, f: A => A, g: A => A): IsEq[S] =
    lens.modify(g)(lens.modify(f)(s)) <==> lens.modify(g compose f)(s)

  def consistentSetModify(s: S, a: A): IsEq[S] =
    lens.set(a)(s) <==> lens.modify(_ => a)(s)

  def consistentModifyModifyId(s: S, f: A => A): IsEq[S] =
    lens.modify(f)(s) <==> lens.modifyF[Id](f)(s)
      def consistentGetModifyId(s: S): IsEq[A] =
    lens.get(s) <==> lens.modifyF[Const[A, ?]](Const(_))(s).getConst
}
```

## Conclusión

Vimos que la inmutabilidad está buena. Pero también vimos que tiene algunos problemas no triviales de resolver. El concepto de lenses resuelva esta situación particular. No es necesario entender los tipos complejos que ofrecen las bibliotecas, alcanza con entender cómo crearlos  y cómo aplicarlas.
Hay mucho más allá afuera sobre lenses, optics, prisms, antiparras y demás. Pero para esta clase no nos interesa mucho. Lo importante es saber que este problema existe, es muy común, y ya hay algo que resuelve este problema (y no es dejar de usar Haskell y pasar a un lenguaje mutable).

### Lecturas recomendadas

- [Tutorial de lens en Haskell](https://hackage.haskell.org/package/lens-tutorial-1.0.4/docs/Control-Lens-Tutorial.html)

- [Monocle para Scala](https://www.optics.dev/Monocle/)
