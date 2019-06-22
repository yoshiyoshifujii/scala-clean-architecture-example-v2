package adapters.http.json

final case class AccountGetsResponseJson(accounts: Seq[AccountJson], error_messages: Seq[String] = Seq.empty)
    extends ResponseJson
