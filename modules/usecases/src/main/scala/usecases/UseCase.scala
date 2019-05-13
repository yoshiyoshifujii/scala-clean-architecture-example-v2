package usecases

trait UseCase[F[_], InputData, OutputData] {

  def execute(inputData: InputData): F[OutputData]

}
