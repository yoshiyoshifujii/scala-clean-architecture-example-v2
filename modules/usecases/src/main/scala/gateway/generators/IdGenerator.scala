package gateway.generators

private[generators] trait IdGenerator[F[_], A] {
  def generate: F[A]
}
