package adapters.http.json

import domain.account.Auth

final case class AccountUpdateRequestJsonWithAuth(auth: Auth, request: AccountUpdateRequestJson, accountId: String)
final case class AccountUpdateRequestJson(name: String)
