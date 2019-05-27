package adapters.http.json

final case class ErrorResponseJson(error_messages: Seq[String]) extends ResponseJson
