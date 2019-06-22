package adapters.http.json

import domain.account.Auth

final case class AccountGetRequestWithAuth(auth: Auth, accountId: String)
