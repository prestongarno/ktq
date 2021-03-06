/*
 * Copyright (C) 2018 Preston Garno
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

package com.prestongarno.kotlinq.compiler

import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

class InterfaceMultiInheritance : JavacTest() {

  @Ignore // TODO -> this is a very important test to have, leaving it in but disabled
  @Test fun `multi inherited field is assigned superinterfaces from all superinterface argbuilder types`() {

    val schema = """
      |
      |interface Entity {
      |  friends(query: String!, first: Int, after: ID): [Entity]
      |}
      |
      |scalar ID
      |
      |interface Actor {
      |  friends(first: Int, after: ID): [Entity]
      |}
      |
      |type Wookie implements Entity, Actor {
      |  friends(query: String!, first: Int, after: ID): [Entity]
      |}
      |
      |""".trimMargin("|")

    assertThat(compileOut(schema).minusPackageNames()).isEqualTo("""
        |
        |interface Entity : QType, QInterface {
        |  val friends: InterfaceListStub.ConfigurableQuery<Entity, out Entity.BaseFriendsArgs>
        |
        |  interface BaseFriendsArgs : ArgumentSpec {
        |    abstract val query: String
        |
        |    abstract var first: Int?
        |
        |    abstract var after: ID?
        |  }
        |}
        |
        |
        |object ID : CustomScalar
        |
        |
        |interface Actor : QType, QInterface {
        |  val friends: InterfaceListStub.ConfigurableQuery<Entity, out Actor.BaseFriendsArgs>
        |
        |  interface BaseFriendsArgs : ArgumentSpec {
        |    abstract var first: Int?
        |
        |    abstract var after: ID?
        |  }
        |}
        |
        |
        |object Wookie : QType, Entity, Actor {
        |  override val friends: InterfaceListStub.ConfigurableQuery<Entity, FriendsArgs> by QInterfaces.List.configured<Entity, FriendsArgs>()
        |
        |  class FriendsArgs(query: String) : ArgBuilder(), Entity.BaseFriendsArgs, Actor.BaseFriendsArgs {
        |    override val query: String by arguments.notNull<String>("query", query)
        |
        |    override var first: Int? by arguments
        |
        |    override var after: ID? by arguments
        |  }
        |}
        |""".trimMargin("|"))

    jvmCompileAndLoad(schema, "com.star.wars").apply {
      loadClass("com.star.wars.Actor\$BaseFriendsArgs").isAbstract eq true
    }
  }
}


private fun String.minusPackageNames() = this.replace("com.prestongarno.kotlinq.", "")
    .replace("stubs.", "")
    .replace("core.", "")
    .replace("QSchemaType.", "")

