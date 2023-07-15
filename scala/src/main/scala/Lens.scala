package tadp.lenses

case class Lens[O, V](
                       get: O => V,
                       set: (O, V) => O
                     )
object Lens {
  def compose[Outer, Inner, Value](
                                    outer: Lens[Outer, Inner],
                                    inner: Lens[Inner, Value]
                                  ): Lens[Outer, Value] = Lens[Outer, Value](
    get = outer.get andThen inner.get,
    set = (obj, value) => outer.set(obj, inner.set(outer.get(obj), value))
  )

  def getSet[S, A](lens: Lens[S, A], s: S) = identity(lens, s)

  def identity[S, A](lens: Lens[S, A], s: S): Boolean =
    lens.set(s, lens.get(s)) == s

  def setGet[S, A](lens: Lens[S, A], s: S, a: A) = retention(lens, s, a)

  def retention[S, A](lens: Lens[S, A], s: S, a: A): Boolean =
    lens.get(lens.set(s, a)) == a

  def putPut[S, A](lens: Lens[S, A], s: S, a: A, b: A) = doubleSet(lens, s, a, b)

  def doubleSet[S, A](lens: Lens[S, A], s: S, a: A, b: A): Boolean =
    lens.get(lens.set(lens.set(s, a), b)) == b
}

case class Prism[S, A](_getOption: S => Option[A])(_reverseGet: A => S) {
  def getOption(s: S): Option[A] = _getOption(s)
  def reverseGet(a: A): S = _reverseGet(a)
}

case class Optional[S, A](_getOption: S => Option[A])(_set: A => S => S){
  def getOption(s: S): Option[A] = _getOption(s)
  def set(a: A): S => S = _set(a)
}
