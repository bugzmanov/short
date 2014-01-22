package bugzmanov.shortner.backend

class SqlDBKeyRepositorySpec extends KeyedRepositorySpec{
  val repo: SqlDBKeyRepository = new SqlDBKeyRepository()
  repo.createSchema()
}
