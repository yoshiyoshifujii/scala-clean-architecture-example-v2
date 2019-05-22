package domain.account

import com.github.j5ik2o.dddbase.Aggregate
import domain.common.Email

import scala.reflect._

sealed trait Account extends Aggregate {
  override type AggregateType = Account
  override type IdType        = AccountId
  override protected val tag: ClassTag[Account] = classTag[Account]

  val email: Email
  val name: AccountName
  val password: EncryptedPassword
}

case class GeneratedAccount private[account] (
    id: AccountId,
    email: Email,
    name: AccountName,
    password: EncryptedPassword
) extends Account

case class ResolvedAccount private[account] (
    id: AccountId,
    email: Email,
    name: AccountName,
    password: EncryptedPassword
) extends Account

object Account {

  val generate: (AccountId, Email, AccountName, EncryptedPassword) => GeneratedAccount = GeneratedAccount.apply

  val generateResolved: (AccountId, Email, AccountName, EncryptedPassword) => ResolvedAccount = ResolvedAccount.apply

  val rename: (ResolvedAccount, AccountName) => ResolvedAccount = (account, name) => account.copy(name = name)

}
