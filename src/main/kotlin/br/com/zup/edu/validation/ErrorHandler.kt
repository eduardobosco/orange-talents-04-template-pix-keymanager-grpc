package br.com.zup.edu.validation

import br.com.zup.edu.errors.handler.ExceptionHandlerInterceptor
import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationTarget.*
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(CLASS, FILE, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Around
@Type(ExceptionHandlerInterceptor::class)

annotation class ErrorHandler()