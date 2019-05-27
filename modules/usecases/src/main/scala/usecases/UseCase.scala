package usecases

trait UseCase[F[_], InputData, OutputData] {

  def execute(inputData: InputData)(implicit ME: UseCaseMonadError[F]): F[OutputData]

}
