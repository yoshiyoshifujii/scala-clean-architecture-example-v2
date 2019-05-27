package adapters.dao.jdbc

trait AccountComponent extends SlickDaoSupport {

  import profile.api._

  case class AccountRecord(
      id: String,
      name: String,
      password: String,
      email: String
  ) extends Record

  case class Accounts(tag: Tag) extends TableBase[AccountRecord](tag, "account") {
    def id: Rep[String]       = column[String]("id")
    def name: Rep[String]     = column[String]("name")
    def password: Rep[String] = column[String]("password")
    def email: Rep[String]    = column[String]("email")
    def pk                    = primaryKey("pk", (id))
    override def *            = (id, name, password, email) <> (AccountRecord.tupled, AccountRecord.unapply)
  }

  object AccountDao extends TableQuery(Accounts)

}
