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

package com.prestongarno.kotlinq.core

import com.prestongarno.kotlinq.core.hooks.Fragment
import com.prestongarno.kotlinq.core.internal.bracket
import com.prestongarno.kotlinq.core.internal.stringify
import com.prestongarno.kotlinq.core.properties.FragmentProvider

/**
 * Interface representing a *concrete* type on a graphql schema.
 */
interface QType : QSchemaType

/**
 * Interface API entry for a fragment requires
 * type intersection with [QInterface] type and a [QType]
 *
 * Not perfect but does the job
 */
interface QInterface : QSchemaType

/**
 * Supertype of a GraphQL Enum definition
 *
 * This isn't forced to be an actual native enum. However,
 * the API entrypoints require multiple bounds, one of which being [kotlin.Enum] */
interface QEnumType : QSchemaType

/**
 * Interface for all Custom Scalar types fragment a schema.
 * Supports custom deserialization or mapping to another object by
 * using {@link com.prestongarno.ktq.adapters.custom.QScalarMapper}
 */
interface CustomScalar : QSchemaType

/**
 * Union type. Generated object class-level delegates to the private implementation below.
 * Only way to hook into a fragmenting method for graphql union fragments on queries
 */
interface QUnionType : QType {

  val queue: FragmentProvider

  fun on(init: () -> QModel<QType>)

  companion object {
    fun new(): QUnionType = QUnionTypeImpl()
  }

  private
  class QUnionTypeImpl : QUnionType {

    override val queue: FragmentProvider = FragmentProvider()

    override fun on(init: () -> QModel<QType>) {
      queue.addFragment(Fragment(init))
    }
  }
}

/**
 * Interface for all Input Objects.
 *
 * NOTE: Generated schema type is not a static object
 * like the other types. This is a class, so perfectly
 * fine to bind to an instance
 *
 * @author prestongarno
 * @since 0.3.0
 */
interface QInputType : QSchemaType {
  val input: PropertyMapper

  companion object {

    /**
     * Utility function to create a new type for class-level
     * delegation from the compiled/generated schema
     */
    fun new(): QInputType = object : QInputType {

      private
      val field = PropertyMapper()

      override
      val input: PropertyMapper
        get() = this.field

      /**
       * TODO -> [com.prestongarno.ktq.internal.formatAs] is used in
       * [com.prestongarno.ktq.internal.stringify] and allows [QModel]
       * values in the backing map. This is illegal in the GraphQL specificaton
       * for input object types
       */
      override
      fun toString(): String = field.stringify().bracket()
    }
  }
}