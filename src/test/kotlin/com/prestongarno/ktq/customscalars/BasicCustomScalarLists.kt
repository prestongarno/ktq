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

package com.prestongarno.ktq.customscalars

import com.prestongarno.ktq.QModel
import com.prestongarno.ktq.QSchemaType
import com.prestongarno.ktq.QType
import com.prestongarno.ktq.adapters.custom.StringScalarListMapper
import com.prestongarno.ktq.primitives.eq
import org.junit.Test

object ResourceBundle : QType {
  val urls by QSchemaType.QCustomScalarList.stub<URL>()
}

class BasicCustomScalarLists {

  @Test fun `custom scalar list is possible`() {

    val query = object : QModel<ResourceBundle>(ResourceBundle) {
      val urls by model.urls.map(StringScalarListMapper { it })
    }
    query::urls.returnType.arguments
        .firstOrNull()?.type?.classifier eq String::class
    query.toGraphql(false) eq "{urls}"
  }
}