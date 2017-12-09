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

package com.prestongarno.kotlinq.core.customscalars

import com.prestongarno.kotlinq.core.QModel
import com.prestongarno.kotlinq.core.QSchemaType
import com.prestongarno.kotlinq.core.QType
import com.prestongarno.kotlinq.core.adapters.custom.StringScalarListMapper
import com.prestongarno.kotlinq.core.primitives.eq
import org.junit.Test

object ResourceBundle : QType {
  val urls by QSchemaType.QCustomScalar.List.stub<URL>()
}

class BasicCustomScalarLists {

  @Test fun `custom scalar list is possible`() {

    val query = object : QModel<ResourceBundle>(ResourceBundle) {
      val urls by model.urls.map(StringScalarListMapper { it })
    }
    query::urls.returnType.arguments
        .firstOrNull()?.type?.classifier eq String::class
    query.toGraphql() eq "{urls}"
  }
}