/*
 * Copyright (C) 2017 Preston Garno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ktq.unions.experimental


/*
class UnionToGraphql {

  @Ignore @Test fun initializeSchemaModelForFile() {
    QCompiler.initialize()
        .packageName("com.prestongarno.ktq")
        .schema("""
          |
          |interface Actor {
          |  login: String
          |}
          |
          |type User implements Actor {
          |  login: String
          |  email: String?
          |  repositories: [Repository]
          |}
          |
          |type Organization implements Actor {
          |  login: String
          |  members: [User]
          |  owner: User
          |  repositories: [Repository]
          |}
          |
          |createUnionStub Account = User | Bot | Organization
          |
          |type Bot implements Actor {
          |  login: String
          |  owner: User
          |}
          |
          |scalar URL
          |
          |type Repository {
          |  name: String
          |  url: URL
          |}
          |
          |type Query {
          |  searchAccounts(first: Int, searchTerm: String!): [Account]
          |}
          |
          """.trimMargin("|"))
        .compile()
        .result { println(it) }
  }

  @Ignore @Test fun testUnionToGraphqlIsCorrect() {

    val userModelInitializer = {
      object : QModel<User>(User) {
        val login by User.login
      }
    }
    val organizationModelInitializer = {
      object : QModel<Organization>(Organization) {
        val login by Organization.login
        val members by Organization.members.querying { userModelInitializer() }
      }
    }

    val queryModel = object : QModel<Query>(Query) {
      val accountSearch by Query.searchAccounts.scope {
        first(10)
        searchTerm("google.com")
      }.querying {
        object : QModel<Account>(Account) {
          val organizations by Account.Organization.querying { organizationModelInitializer() }
        }
      }
    }

    assertThat(queryModel.toGraphql())
        .isEqualTo("""
          |{
          |  searchAccounts(first: 10,searchTerm: \"google.com\"){
          |    ... fragment Organization{
          |      login,
          |      members{
          |        login
          |      }
          |    }
          |  }
          |}
          """.trimMargin("|"))

  }

  @Ignore @Test fun multipleUnionFields() {

    val userModelInitializer = {
      object : QModel<User>(User) {
        val login by User.login
      }
    }
    val organizationModelInitializer = {
      object : QModel<Organization>(Organization) {
        val login by Organization.login
        val members by Organization.members.querying { userModelInitializer() }
      }
    }

    val queryModel = object : QModel<Query>(Query) {
      val accountSearch by Query.searchAccounts.scope {
        first(10)
        searchTerm("google.com")
      }.querying {
        object : QModel<Account>(Account) {
          val organizations by Account.Organization.querying { organizationModelInitializer() }

          val users by Account.User.querying { userModelInitializer() }
        }
      }
    }

    assertThat(queryModel.toGraphql())
        .isEqualTo("""
          |{
          |  searchAccounts(first: 10,searchTerm: \"google.com\"){
          |    ... fragment Organization{
          |      login,
          |      members{
          |        login
          |      }
          |    },
          |    ... fragment User{
          |      login
          |    }
          |  }
          |}
          """.trimMargin("|"))
  }

  @Ignore @Test fun tripleUnionFields() {

    val userModelInitializer = {
      object : QModel<User>(User) {
        val login by User.login
      }
    }
    val botModelInitializer = {
      object : QModel<Bot>(Bot) {
        val login by Bot.login
        val owner by Bot.owner.querying { userModelInitializer() }
      }
    }
    val organizationModelInitializer = {
      object : QModel<Organization>(Organization) {
        val login by Organization.login
        val members by Organization.members.querying { userModelInitializer() }
      }
    }

    val queryModel = object : QModel<Query>(Query) {
      val accountSearch by Query.searchAccounts.scope {
        first(10)
        searchTerm("google.com")
      }.querying {
        object : QModel<Account>(Account) {
          val organizations by Account.Organization.querying { organizationModelInitializer() }
          val users by Account.User.querying { userModelInitializer() }
          val bots by Account.Bot.querying { botModelInitializer() }
        }
      }
    }

    assertThat(queryModel.toGraphql())
        .isEqualTo("""
          |{
          |  searchAccounts(first: 10,searchTerm: \"google.com\"){
          |    ... fragment Organization{
          |      login,
          |      members{
          |        login
          |      }
          |    },
          |    ... fragment User{
          |      login
          |    },
          |    ... fragment Bot{
          |      login,
          |      owner{
          |        login
          |      }
          |    }
          |  }
          |}
          """.trimMargin("|"))
  }
}

object Account : QSchemaUnion by QSchemaUnion.create(Account) {
  val User: ListInitStub<User> by QTypeList.stub()

  val Bot: ListInitStub<Bot> by QTypeList.stub()

  val Organization: ListInitStub<Organization> by QTypeList.stub()
}

interface Actor : QSchemaType {
  val login: Stub<String>
}

object Bot : QSchemaType, Actor {
  override val login: Stub<String> by QScalar.stringStub()

  val owner: InitStub<User> by QType.stub()
}

object Organization : QSchemaType, Actor {
  override val login: Stub<String> by QScalar.stubPrimitive()

  val members: ListInitStub<User> by QTypeList.stub()

  val owner: InitStub<User> by QType.stub()

  val repositories: ListInitStub<Repository> by QTypeList.stub()
}

object Query : QSchemaType {
  val searchAccounts: ListConfigType<Account, SearchAccountsArgs> by QTypeList.stub { SearchAccountsArgs(it) }

  class SearchAccountsArgs(args: TypeListArgBuilder) : TypeListArgBuilder by args {
    fun first(value: Int): SearchAccountsArgs = apply { addArg("first", value) }
    fun searchTerm(value: String): SearchAccountsArgs = apply { addArg("searchTerm", value) }
  }
}

object Repository : QSchemaType {
  val name: Stub<String> by QScalar.stubPrimitive()

  val url: CustomScalarInitStub<URL> by QCustomScalar.stub()
}

object URL : CustomScalar

object User : QSchemaType, Actor {
  override val login: Stub<String> by QScalar.stubPrimitive()

  val email: Stub<String> by QScalar.stubPrimitive()

  val repositories: ListInitStub<Repository> by QTypeList.stub()
}*/