package adapters.gateway.repositories.slick

import adapters.AppType
import adapters.dao.jdbc.RDB
import adapters.util.{ FlywayWithMySQLSpecSupport, Slick3SpecSupport }
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import zio.internal.{ Platform, PlatformLive }

class AccountRepositoryBySlickSpec
    extends FreeSpec
    with FlywayWithMySQLSpecSupport
    with Slick3SpecSupport
    with DiagrammedAssertions {
  override val tables: Seq[String] = Seq("account")

  "AccountRepositoryBySlick" - {
    def runtime = new zio.Runtime[AppType] {
      override val Environment: AppType = new RDB.Live(dbConfig.profile, dbConfig.db)
      val Platform: Platform            = PlatformLive.Default
    }

    "findBy is empty, store, findBy non empty, delete." in {
      val email = Email.generate("a@a.com")

      val repository = new AccountRepositoryBySlick(dbConfig.profile, dbConfig.db)

      assert {
        runtime.unsafeRun {
          repository.findBy(email)
        }.isEmpty
      }

      val accountId   = AccountId()
      val accountName = AccountName.generate("hoge hogeo")
      val password    = EncryptedPassword("passPass1")
      val account     = Account.generate(accountId, email, accountName, password)

      assert {
        runtime.unsafeRun {
          repository.store(account)
        } === 1L
      }

      val result = runtime.unsafeRun {
        repository.findBy(email)
      }
      assert(result.nonEmpty)
      assert(result.get.id === accountId)
      assert(result.get.email === email)
      assert(result.get.name === accountName)
      assert(result.get.password === password)

      assert {
        val a = runtime.unsafeRun(repository.hardDelete(accountId))
        a === 1L
      }
      assert {
        val a = runtime.unsafeRun(repository.hardDelete(accountId))
        a === 0L
      }
    }

  }
}
