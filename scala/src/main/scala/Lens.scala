package tadp.lenses

object ReflectiveLens {
  def apply[U, T](fieldName: String): Lens[U, T] = Lens[U,T](
    get = (u: U) => u.getClass.getField(fieldName).get().asInstanceOf[T],
    set = (u: U, t: T) => {
      val copy = u.getClass.getMethod("copy").invoke(u)
      copy.getClass.getField(fieldName).set(copy, t)
      copy.asInstanceOf[U]
    }
  )
}

case class Lens[U, T](get: U => T, set: (U, T) => U) {
  def compose[V](other: Lens[T,V]): Lens[U, V] = this $ other

  // outer -> innerLens -> valor
  def $[V](other: Lens[T,V]): Lens[U, V] = Lens[U,V](
    get = this.get andThen other.get,
    set = (obj, value) => this.set(obj, other.set(this.get(obj),value))
  )
}
object Lens {
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

