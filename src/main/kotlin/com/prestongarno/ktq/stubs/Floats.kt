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

package com.prestongarno.ktq.stubs

import com.prestongarno.ktq.ArgBuilder
import com.prestongarno.ktq.QModel
import com.prestongarno.ktq.SchemaStub
import com.prestongarno.ktq.adapters.applyNotNull
import com.prestongarno.ktq.adapters.bind
import com.prestongarno.ktq.adapters.toMap
import com.prestongarno.ktq.properties.GraphQlProperty
import kotlin.reflect.KProperty

interface FloatDelegate<out A : ArgBuilder> : ScalarDelegate<FloatStub> {

  var default: Float

  fun config(scope: A.() -> Unit)

  companion object {

    internal fun noArgStub(
        qproperty: GraphQlProperty
    ): FloatDelegate.Query = QueryImpl(qproperty)

    internal fun <A : ArgBuilder> optionalArgStub(
        qproperty: GraphQlProperty
    ): FloatDelegate.OptionalConfigQuery<A> =
        OptionalConfigQueryImpl(qproperty)

    internal fun <A : ArgBuilder> argStub(
        qproperty: GraphQlProperty
    ): FloatDelegate.ConfigurableQuery<A> =
        ConfigurableQueryImpl(qproperty)

  }

  interface Query : SchemaStub {

    operator fun invoke(
        arguments: ArgBuilder? = null,
        scope: (FloatDelegate<ArgBuilder>.() -> Unit)? = null
    ): FloatDelegate<ArgBuilder>

    operator fun provideDelegate(
        inst: QModel<*>,
        property: KProperty<*>
    ): FloatStub = invoke().provideDelegate(inst, property)
  }

  interface OptionalConfigQuery<A : ArgBuilder> : SchemaStub {

    operator fun invoke(
        arguments: A,
        scope: (FloatDelegate<A>.() -> Unit)?
    ): FloatDelegate<A>

    operator fun provideDelegate(
        inst: QModel<*>,
        property: KProperty<*>
    ): FloatStub
  }

  interface ConfigurableQuery<A : ArgBuilder> : SchemaStub {

    operator fun invoke(
        arguments: A,
        scope: (FloatDelegate<A>.() -> Unit)? = null
    ): FloatDelegate<A>
  }

  /*********************************************************************************
   * Private default implementations
   */
  private class QueryImpl(val qproperty: GraphQlProperty) : FloatDelegate.Query {
    override fun invoke(
        arguments: ArgBuilder?, scope: (FloatDelegate<ArgBuilder>.() -> Unit)?
    ) = FloatDelegateImpl(qproperty, arguments ?: ArgBuilder()).applyNotNull(scope)
  }

  private class OptionalConfigQueryImpl<A : ArgBuilder>(
      val qproperty: GraphQlProperty
  ) : FloatDelegate.OptionalConfigQuery<A> {

    override fun invoke(
        arguments: A,
        scope: (FloatDelegate<A>.() -> Unit)?
    ): FloatDelegate<A> =
        FloatDelegateImpl(qproperty, arguments).applyNotNull(scope)

    override fun provideDelegate(
        inst: QModel<*>,
        property: KProperty<*>
    ): FloatStub = FloatStub(qproperty).bind(inst)
  }

  private class ConfigurableQueryImpl<A : ArgBuilder>(
      val qproperty: GraphQlProperty
  ) : FloatDelegate.ConfigurableQuery<A> {

    override fun invoke(
        arguments: A,
        scope: (FloatDelegate<A>.() -> Unit)?
    ): FloatDelegate<A> =
        FloatDelegateImpl(qproperty, arguments).applyNotNull(scope)
  }
}

private class FloatDelegateImpl<out A : ArgBuilder>(
    val qproperty: GraphQlProperty,
    val argBuilder: A? = null
) : FloatDelegate<A> {

  override var default: Float = 0f

  override fun config(scope: A.() -> Unit) {
    argBuilder?.scope()
  }

  override fun provideDelegate(inst: QModel<*>, property: KProperty<*>): FloatStub =
      FloatStub(qproperty, argBuilder.toMap(), default).bind(inst)
}
