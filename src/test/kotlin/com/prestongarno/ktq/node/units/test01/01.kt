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

package com.prestongarno.ktq.node.units.test01

import com.prestongarno.ktq.compiler.QCompiler
import org.junit.Test
import com.prestongarno.ktq.node.server.NodeServer
import org.junit.Ignore
import kotlin.reflect.KProperty

class TestOne : NodeServer() {

  override val serverNumber: Int = 1

  @Ignore @Test fun gen() {
    QCompiler.initialize().schema("""
        |
        |type Query {
        |  me: Actor
        |}
        |
        |union Actor = User | Bot
        |
        |type User {
        |  name: String
        |}
        |
        |type Bot {
        |  name: String
        |  owner: Actor
        |}
        |""".trimMargin("|"))
        .compile().result { println(it) }
  }

  // Need to redesign the current createUnionStub type model in order to
  // support single fields
  @Ignore @Test fun uglyDesignMistakeOnUnions() {

  }

}

/*****************************************************************/
/*****************************************************************/

