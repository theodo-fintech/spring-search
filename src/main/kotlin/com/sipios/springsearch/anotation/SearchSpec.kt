package com.sipios.springsearch.anotation

/**
 * An annotation for mapping query search string into a Specification for a JPA Domain
 *
 * This annotation can be used on a parameter in a Spring MVC RestRepository Class
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SearchSpec(
    /**
     * The name of the query param that will be transformed into a specification
     */
    val searchParam: String = "search"
)
