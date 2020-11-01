package com.sipios.springsearch

import org.springframework.data.jpa.domain.Specification
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.persistence.criteria.*

/**
 * Implementation of the JPA Specification based on a Search Criteria
 *
 * @see Specification
 *
 * @param <T> THe class on which the specification will be applied
</T> */
class SpecificationImpl<T>(private val criteria: SearchCriteria) : Specification<T> {
    override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, builder: CriteriaBuilder): Predicate? {
        val nestedKey = criteria.key.split(".")
        val nestedRoot = getNestedRoot(root, nestedKey)
        val criteriaKey = nestedKey[nestedKey.size - 1]

        val javaType = root.get<T>(criteriaKey).javaType

        if (criteria.value!!.equals("null", true)) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.isNull(nestedRoot.get<Objects>(criteriaKey))
                SearchOperation.NOT_EQUALS -> builder.isNotNull(nestedRoot.get<Objects>(criteriaKey))
                else -> null
            }
        }

        if (criteria.value!!.equals("empty", true)) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.isEmpty(nestedRoot.get(criteriaKey))
                SearchOperation.NOT_EQUALS -> builder.isNotEmpty(nestedRoot.get(criteriaKey))
                else -> null
            }
        }

        if (javaType == java.time.LocalTime::class.java) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.equal(nestedRoot.get<LocalTime>(criteriaKey), LocalTime.parse(criteria.value))
                SearchOperation.NOT_EQUALS -> builder.notEqual(nestedRoot.get<LocalTime>(criteriaKey), LocalTime.parse(criteria.value))
                SearchOperation.GREATER_THAN -> builder.greaterThan(nestedRoot.get<LocalTime>(criteriaKey), LocalTime.parse(criteria.value))
                SearchOperation.LESS_THAN -> builder.lessThan(nestedRoot.get<LocalTime>(criteriaKey), LocalTime.parse(criteria.value))
                else -> null
            }
        }

        if (javaType == java.time.LocalDateTime::class.java) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.equal(nestedRoot.get<LocalDateTime>(criteriaKey), LocalDateTime.parse(criteria.value))
                SearchOperation.NOT_EQUALS -> builder.notEqual(nestedRoot.get<LocalDateTime>(criteriaKey), LocalDateTime.parse(criteria.value))
                SearchOperation.GREATER_THAN -> builder.greaterThan(nestedRoot.get<LocalDateTime>(criteriaKey), LocalDateTime.parse(criteria.value))
                SearchOperation.LESS_THAN -> builder.lessThan(nestedRoot.get<LocalDateTime>(criteriaKey), LocalDateTime.parse(criteria.value))
                else -> null
            }
        }

        if (javaType == java.time.Duration::class.java) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.equal(nestedRoot.get<Duration>(criteriaKey), Duration.parse(criteria.value))
                SearchOperation.NOT_EQUALS -> builder.notEqual(nestedRoot.get<Duration>(criteriaKey), Duration.parse(criteria.value))
                SearchOperation.GREATER_THAN -> builder.greaterThan(nestedRoot.get<Duration>(criteriaKey), Duration.parse(criteria.value))
                SearchOperation.LESS_THAN -> builder.lessThan(nestedRoot.get<Duration>(criteriaKey), Duration.parse(criteria.value))
                else -> null
            }
        }

        if (javaType == java.sql.Date::class.java || javaType == java.util.Date::class.java) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.equal(nestedRoot.get<Date>(criteriaKey), Date(criteria.value!!.toLong()))
                SearchOperation.NOT_EQUALS -> builder.notEqual(nestedRoot.get<Date>(criteriaKey), Date(criteria.value!!.toLong()))
                SearchOperation.GREATER_THAN -> builder.greaterThan(nestedRoot.get<Date>(criteriaKey), Date(criteria.value!!.toLong()))
                SearchOperation.LESS_THAN -> builder.lessThan(nestedRoot.get<Date>(criteriaKey), Date(criteria.value!!.toLong()))
                else -> null
            }
        }

        if (javaType == java.lang.Boolean::class.java) {
            return when (criteria.operation) {
                SearchOperation.EQUALS -> builder.equal(nestedRoot.get<Boolean>(criteriaKey), criteria.value!!.toBoolean())
                SearchOperation.NOT_EQUALS -> builder.notEqual(nestedRoot.get<Boolean>(criteriaKey), criteria.value!!.toBoolean())
                else -> null
            }
        }

        return when (criteria.operation) {
            SearchOperation.EQUALS -> builder.equal(nestedRoot.get<String>(criteriaKey), criteria.value)
            SearchOperation.NOT_EQUALS -> builder.notEqual(nestedRoot.get<String>(criteriaKey), criteria.value)
            SearchOperation.GREATER_THAN -> builder.greaterThan(nestedRoot.get<Double>(criteriaKey), criteria.value!!.toDouble())
            SearchOperation.LESS_THAN -> builder.lessThan(nestedRoot.get<Double>(criteriaKey), criteria.value!!.toDouble())
            SearchOperation.STARTS_WITH -> builder.like(nestedRoot.get(criteriaKey), criteria.value + "%")
            SearchOperation.ENDS_WITH -> builder.like(nestedRoot.get(criteriaKey), "%" + criteria.value)
            SearchOperation.CONTAINS -> builder.like(builder.lower(nestedRoot.get<String>(criteriaKey).`as`(String::class.java)), "%" + criteria.value + "%")
            SearchOperation.DOESNT_START_WITH -> builder.notLike(nestedRoot.get(criteriaKey), criteria.value + "%")
            SearchOperation.DOESNT_END_WITH -> builder.notLike(nestedRoot.get(criteriaKey), "%" + criteria.value)
            SearchOperation.DOESNT_CONTAIN -> builder.notLike(builder.lower(nestedRoot.get<String>(criteriaKey).`as`(String::class.java)), "%" + criteria.value + "%")
            null -> null;
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