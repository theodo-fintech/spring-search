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
        if (ops == SearchOperation.IS && value != null) {
            return builder.isEmpty(path[fieldName])
        }
        if (ops == SearchOperation.IS_NOT && value != null) {
            return builder.isNotEmpty(path[fieldName])
        }
        throw IllegalArgumentException("Unsupported operation $ops for collection field $fieldName, only IS and IS_NOT are supported")
    }
}
