package adapters.http.json

trait ResponseJson {
  def error_messages: Seq[String]
  def isSuccessful: Boolean = error_messages.isEmpty
}
