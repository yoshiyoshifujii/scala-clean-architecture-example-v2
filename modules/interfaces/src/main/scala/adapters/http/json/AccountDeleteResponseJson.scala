package adapters.http.json

final case class AccountDeleteResponseJson(error_messages: Seq[String] = Seq.empty) extends ResponseJson
