package com.prestongarno.ktq.stubs

import com.prestongarno.ktq.ArgBuilder
import com.prestongarno.ktq.DelegateProvider
import com.prestongarno.ktq.QEnumType
import com.prestongarno.ktq.QModel
import com.prestongarno.ktq.SchemaStub
import com.prestongarno.ktq.adapters.QField
import com.prestongarno.ktq.adapters.applyNotNull
import com.prestongarno.ktq.adapters.newEnumListDelegate
import com.prestongarno.ktq.adapters.newEnumListField
import com.prestongarno.ktq.properties.GraphQlProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface EnumListStub<T, out A> : DelegateProvider<List<T>> where T : Enum<*>, T : QEnumType, A : ArgBuilder {

  var default : T?

  fun config(scope: A.() -> Unit)

  companion object {

    @PublishedApi internal fun <T> noArgStub(
        qproperty: GraphQlProperty,
        enumClass: KClass<T>
    ): EnumListStub.Query<T> where T : Enum<*>, T : QEnumType =
        QueryImpl<T>(qproperty, enumClass)

    @PublishedApi internal fun <T, A> optionalArgStub(
        qproperty: GraphQlProperty,
        enumClass: KClass<T>
    ): EnumListStub.OptionalConfigQuery<T, A>
        where T : Enum<*>, T : QEnumType, A : ArgBuilder =
        OptionalConfigQueryImpl(qproperty, enumClass)

    @PublishedApi internal fun <T, A> argStub(
        qproperty: GraphQlProperty,
        enumClass: KClass<T>
    ): EnumListStub.ConfigurableQuery<T, A>
        where T : Enum<*>, T : QEnumType, A : ArgBuilder =
        ConfigurableQueryImpl(qproperty, enumClass)
  }

  interface Query<T> : DelegateProvider<List<T>> where T : Enum<*>, T : QEnumType {

    operator fun invoke(
        arguments: ArgBuilder? = null,
        scope: (EnumListStub<T, ArgBuilder>.() -> Unit)? = null
    ): EnumListStub<T, ArgBuilder>

  }

  private class QueryImpl<T>(
      val qproperty: GraphQlProperty,
      val enumClass: KClass<T>
  ) : Query<T> where T : Enum<*>, T : QEnumType {

    override fun provideDelegate(inst: QModel<*>, property: KProperty<*>): QField<List<T>> =
        invoke(ArgBuilder()).provideDelegate(inst, property)

    override fun invoke(
        arguments: ArgBuilder?,
        scope: (EnumListStub<T, ArgBuilder>.() -> Unit)?
    ): EnumListStub<T, ArgBuilder> =
        newEnumListDelegate<T, ArgBuilder>(qproperty, arguments ?: ArgBuilder(), enumClass).applyNotNull(scope)
  }

  interface OptionalConfigQuery<T, A> : DelegateProvider<List<T>> where T : Enum<*>, T : QEnumType, A : ArgBuilder {

    operator fun invoke(
        arguments: A,
        scope: (EnumListStub<T, A>.() -> Unit)? = null
    ): EnumListStub<T, ArgBuilder>

  }

  private class OptionalConfigQueryImpl<T, A>(
      val qproperty: GraphQlProperty,
      val enumClass: KClass<T>
  ): OptionalConfigQuery<T, A> where T : Enum<*>, T : QEnumType, A : ArgBuilder {

    override fun provideDelegate(
        inst: QModel<*>,
        property: KProperty<*>
    ): QField<List<T>> =
        newEnumListField(qproperty, enumClass)

    override fun invoke(
        arguments: A,
        scope: (EnumListStub<T, A>.() -> Unit)?
    ): EnumListStub<T, ArgBuilder> =
        newEnumListDelegate<T, A>(qproperty, arguments, enumClass).applyNotNull(scope)

  }

  interface ConfigurableQuery<T, A> : SchemaStub where T : Enum<*>, T : QEnumType, A : ArgBuilder {

    operator fun invoke(
        arguments: A,
        scope: (EnumListStub<T, A>.() -> Unit)? = null
    ): EnumListStub<T, ArgBuilder>

  }

  private class ConfigurableQueryImpl<T, A>(
      val qproperty: GraphQlProperty,
      val enumClass: KClass<T>
  ): ConfigurableQuery<T, A> where T : Enum<*>, T : QEnumType, A : ArgBuilder {

    override fun invoke(
        arguments: A,
        scope: (EnumListStub<T, A>.() -> Unit)?
    ): EnumListStub<T, ArgBuilder> =
        newEnumListDelegate<T, A>(qproperty, arguments, enumClass).applyNotNull(scope)

  }

}










