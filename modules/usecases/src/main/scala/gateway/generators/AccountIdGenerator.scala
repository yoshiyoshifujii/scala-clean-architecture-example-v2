package gateway.generators

import domain.account.AccountId

trait AccountIdGenerator[F[_]] extends IdGenerator[F, AccountId]
