package com.sipios.springsearch

import com.sipios.springsearch.strategies.BooleanStrategy
import com.sipios.springsearch.strategies.DateStrategy
import com.sipios.springsearch.strategies.DoubleStrategy
import com.sipios.springsearch.strategies.FloatStrategy
import com.sipios.springsearch.strategies.IntStrategy
import com.sipios.springsearch.strategies.ParsingStrategy
import com.sipios.springsearch.strategies.StringStrategy
import java.util.ArrayList
import java.util.Date
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import kotlin.reflect.KClass
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Implementation of the JPA Specification based on a Search Criteria
 *
 * @see Specification
 *
 * @param <T> THe class on which the specification will be applied
</T> */
class SpecificationImpl<T>(private val criteria: SearchCriteria) : Specification<T> {
    @Throws(ResponseStatusException::class)
    override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, builder: CriteriaBuilder): Predicate? {
        val nestedKey = criteria.key.split(".")
        val nestedRoot = getNestedRoot(root, nestedKey)
        val criteriaKey = nestedKey[nestedKey.size - 1]
        val fieldClass = nestedRoot.get<Any>(criteriaKey).javaType.kotlin
        val strategy = getStrategy(fieldClass)
        val value: Any?
        try {
            value = strategy.parse(criteria.value, fieldClass)
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Could not parse input for the field $criteriaKey as a ${fieldClass.simpleName}"
            )
        }

        return strategy.buildPredicate(builder, nestedRoot, criteriaKey, criteria.operation, value)
    }

    private fun getStrategy(fieldClass: KClass<out Any>): ParsingStrategy {
        return when (fieldClass) {
            Boolean::class -> BooleanStrategy()
            Double::class -> DoubleStrategy()
            Float::class -> FloatStrategy()
            Int::class -> IntStrategy()
            Date::class -> DateStrategy()
            else -> StringStrategy()
        }
    }

    private fun getNestedRoot(root: Root<T>, nestedKey: List<String>): Path<*> {
        val prefix = ArrayList(nestedKey)
        prefix.removeAt(nestedKey.size - 1)
        var temp: Path<*> = root
        for (s in prefix) {
            temp = temp.get<T>(s)
        }

        return temp
    }
}
