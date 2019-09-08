package com.sipios.springsearch

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class SpecificationsBuilder<U> {

    private var specs: Specification<U> = NullSpecification()
    private val parser: CriteriaParser<U> = CriteriaParser()

    fun withSearch(search: String): SpecificationsBuilder<U> {
        specs = parser.parse(search)

        return this
    }

    /**
     * This function expect a search string to have ben provided.
     * The search string has been transformed into a Expression Queue with the format: [OR, value>100, AND, value<1000, label:*MONO*]
     *
     * @return A list of specification used to filter the underlying object using JPA specifications
     */
    fun build(): Specification<U> {
        return specs
    }
}

class NullSpecification<T> : Specification<T> {
    override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return null
    }
}
