package com.sipios.srql

import org.springframework.data.jpa.domain.Specification

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import java.util.ArrayList
import java.util.Arrays

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

        when (criteria.operation) {
            SearchOperation.EQUALS -> return builder.equal(nestedRoot.get<String>(criteriaKey), criteria.value)
            SearchOperation.NOT_EQUALS -> return builder.notEqual(nestedRoot.get<String>(criteriaKey), criteria.value)
            SearchOperation.GREATER_THAN -> return builder.greaterThan(nestedRoot.get<Double>(criteriaKey), criteria.value!!.toDouble())
            SearchOperation.LESS_THAN -> return builder.lessThan(nestedRoot.get<Double>(criteriaKey), criteria.value!!.toDouble())
            SearchOperation.STARTS_WITH -> return builder.like(nestedRoot.get(criteriaKey), criteria.value + "%")
            SearchOperation.ENDS_WITH -> return builder.like(nestedRoot.get(criteriaKey), "%" + criteria.value)
            SearchOperation.CONTAINS -> return builder.like(builder.lower(nestedRoot.get<String>(criteriaKey).`as`(String::class.java)), "%" + criteria.value + "%")
            SearchOperation.DOESNT_START_WITH -> return builder.notLike(nestedRoot.get(criteriaKey), criteria.value + "%")
            SearchOperation.DOESNT_END_WITH -> return builder.notLike(nestedRoot.get(criteriaKey), "%" + criteria.value)
            SearchOperation.DOESNT_CONTAIN -> return builder.notLike(builder.lower(nestedRoot.get<String>(criteriaKey).`as`(String::class.java)), "%" + criteria.value + "%")
            else -> return null
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
