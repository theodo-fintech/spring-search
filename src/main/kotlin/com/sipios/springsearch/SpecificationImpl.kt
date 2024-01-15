package com.sipios.springsearch

import com.sipios.springsearch.anotation.SearchSpec
import com.sipios.springsearch.strategies.ParsingStrategy
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.ArrayList
import org.hibernate.query.sqm.tree.domain.SqmPluralValuedSimplePath
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Implementation of the JPA Specification based on a Search Criteria
 *
 * @see Specification
 *
 * @param <T>The class on which the specification will be applied</T>
 * */
class SpecificationImpl<T>(private val criteria: SearchCriteria, private val searchSpecAnnotation: SearchSpec) :
    Specification<T> {
    @Throws(ResponseStatusException::class)
    override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        builder: CriteriaBuilder
    ): Predicate? {
        val nestedKey = criteria.key.split(".")
        val nestedRoot = getNestedRoot(root, nestedKey)
        val criteriaKey = nestedKey[nestedKey.size - 1]
        val fieldClass = nestedRoot.get<Any>(criteriaKey).javaType.kotlin
        val isCollectionField = nestedRoot.get<Any>(criteriaKey) is SqmPluralValuedSimplePath<*>
        val strategy = ParsingStrategy.getStrategy(fieldClass, searchSpecAnnotation, isCollectionField)
        val value: Any?
        try {
            value =
                if (criteria.value is List<*>) {
                    strategy
                        .parse(criteria.value as List<*>, fieldClass)
                } else {
                    strategy
                        .parse(criteria.value?.toString(), fieldClass)
                }
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Could not parse input for the field $criteriaKey as a ${fieldClass.simpleName} with value ${criteria.value}"
            )
        }

        return strategy.buildPredicate(builder, nestedRoot, criteriaKey, criteria.operation, value)
    }

    private fun getNestedRoot(
        root: Root<T>,
        nestedKey: List<String>
    ): Path<*> {
        val prefix = ArrayList(nestedKey)
        prefix.removeAt(nestedKey.size - 1)
        var temp: Path<*> = root
        for (s in prefix) {
            temp = temp.get<T>(s)
        }

        return temp
    }
}
