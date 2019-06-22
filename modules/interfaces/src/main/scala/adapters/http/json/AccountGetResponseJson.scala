package adapters.http.json

final case class AccountGetResponseJson(account: Option[AccountJson], error_messages: Seq[String] = Seq.empty)
    extends ResponseJson
