package adapters.http.json

import domain.account.Auth

final case class AccountDeleteRequestWithAuth(auth: Auth, accountId: String)
