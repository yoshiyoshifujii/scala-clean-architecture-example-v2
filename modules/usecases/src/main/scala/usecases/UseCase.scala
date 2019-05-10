package usecases

trait UseCase[R, InputData, OutputData] {

  def execute(inputData: InputData): UseCaseZIOR[R, OutputData]

}
