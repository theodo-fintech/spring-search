package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import kotlin.reflect.KClass

class CollectionStrategy : ParsingStrategy {

    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        // value is not used in this strategy, IS and IS_NOT are always followed by EMPTY
        value: Any?
    ): Predicate? {
        if (ops == SearchOperation.IS) {
            return builder.isEmpty(path[fieldName])
        }
        if (ops == SearchOperation.IS_NOT) {
            return builder.isNotEmpty(path[fieldName])
        }
        throw IllegalArgumentException("Unsupported operation $ops for collection field $fieldName, only IS and IS_NOT are supported")
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return value
    }
}
