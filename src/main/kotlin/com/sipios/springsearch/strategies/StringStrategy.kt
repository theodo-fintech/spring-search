package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import com.sipios.springsearch.anotation.SearchSpec
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate

class StringStrategy(var searchSpecAnnotation: SearchSpec) : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        var localValue = (value as String)
        var fieldValue: Expression<String>
        if (!searchSpecAnnotation.caseSensitiveFlag) {
            localValue = localValue.toLowerCase()
            fieldValue = builder.lower(path.get(fieldName))
        } else {
            fieldValue = path.get<String>(fieldName)
        }
        if (searchSpecAnnotation.stringFieldFunction.isNotBlank()) {
            fieldValue = builder.function(searchSpecAnnotation.stringFieldFunction, String::class.java, fieldValue)
        }
        return when (ops) {
            SearchOperation.EQUALS -> builder.equal(fieldValue, value)
            SearchOperation.NOT_EQUALS -> builder.notEqual(fieldValue, value)
            SearchOperation.STARTS_WITH -> builder.like(fieldValue, "$localValue%")
            SearchOperation.ENDS_WITH -> builder.like(fieldValue, "%$localValue")
            SearchOperation.CONTAINS -> builder.like((fieldValue), "%$localValue%")
            SearchOperation.DOESNT_START_WITH -> builder.notLike(fieldValue, "$localValue%")
            SearchOperation.DOESNT_END_WITH -> builder.notLike(fieldValue, "%$localValue")
            SearchOperation.DOESNT_CONTAIN -> builder.notLike(fieldValue, "%$localValue%")
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }
}
