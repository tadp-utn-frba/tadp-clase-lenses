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
  def identity[S, A](lens: Lens[S, A], s: S): Boolean =
    lens.set(s, lens.get(s)) == s

  def retention[S, A](lens: Lens[S, A], s: S, a: A): Boolean =
    lens.get(lens.set(s, a)) == a

  def doubleSet[S, A](lens: Lens[S, A], s: S, a: A, b: A): Boolean =
    lens.get(lens.set(lens.set(s, a), b)) == b
}
