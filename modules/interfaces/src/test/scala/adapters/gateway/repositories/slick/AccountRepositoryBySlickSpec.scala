package adapters.gateway.repositories.slick

import adapters.AppType
import adapters.dao.jdbc.RDB
import adapters.util.{ FlywayWithMySQLSpecSupport, Slick3SpecSupport }
import domain.account.{ Account, AccountId, AccountName, EncryptedPassword }
import domain.common.Email
import org.scalatest.{ DiagrammedAssertions, FreeSpec }
import scalaz.zio.internal.{ Platform, PlatformLive }

class AccountRepositoryBySlickSpec
    extends FreeSpec
    with FlywayWithMySQLSpecSupport
    with Slick3SpecSupport
    with DiagrammedAssertions {
  override val tables: Seq[String] = Seq("account")

  "AccountRepositoryBySlick" - {
    def runtime = new scalaz.zio.Runtime[AppType] {
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

    "already exists" in {
      val repository = new AccountRepositoryBySlick(dbConfig.profile, dbConfig.db)
      val id1        = AccountId()
      val id2        = AccountId()

      assert(id1 !== id2)

      assert {
        runtime.unsafeRun {
          repository.store(
            Account.generate(
              id1,
              Email.generate("a@a.com"),
              AccountName.generate("hoge hogeo"),
              EncryptedPassword("passPass1")
            )
          )
        } === 1L
      }

      assertThrows[scalaz.zio.FiberFailure] {
        runtime.unsafeRun {
          repository.store(
            Account.generate(
              id2,
              Email.generate("a@a.com"),
              AccountName.generate("fuga fugao"),
              EncryptedPassword("passPass1")
            )
          )
        }
      }

      val res = runtime.unsafeRun {
        repository.resolveAll
      }
      assert {
        res.length === 1
      }
      assert {
        res.head.id === id1
      }
      assert {
        res.head.name.value.value === "hoge hogeo"
      }

    }
  }
}
