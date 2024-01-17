package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate

class CollectionStrategy : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        if (ops == SearchOperation.IS && value == null) {
            return builder.isNull(path.get<Any>(fieldName))
        }
        if (ops == SearchOperation.IS && value == SearchOperation.EMPTY) {
            return builder.isEmpty(path[fieldName])
        }
        if (ops == SearchOperation.IS_NOT && value == null) {
            return builder.isNotNull(path.get<Any>(fieldName))
        }
        if (ops == SearchOperation.IS_NOT && value == SearchOperation.EMPTY) {
            return builder.isNotEmpty(path[fieldName])
        }

        throw IllegalArgumentException(
            "Unsupported operation $ops $value for collection field $fieldName, " +
                "only IS null/EMPTY and IS NOT null/EMPTY are supported"
        )
    }
}
