package adapters.http.json

final case class AccountUpdateResponseJson(id: Option[String], error_messages: Seq[String] = Seq.empty)
    extends ResponseJson
