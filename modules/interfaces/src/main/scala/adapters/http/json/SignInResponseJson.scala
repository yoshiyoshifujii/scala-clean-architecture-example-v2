package adapters.http.json

final case class SignInResponseJson(token: Option[String], error_messages: Seq[String] = Seq.empty) extends ResponseJson
