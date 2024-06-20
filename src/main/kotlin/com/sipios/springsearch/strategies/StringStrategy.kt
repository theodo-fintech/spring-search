package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import com.sipios.springsearch.anotation.SearchSpec
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.util.Locale

data class StringStrategy(var searchSpecAnnotation: SearchSpec) : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        if (value !is List<*>) {
            // if case-sensitive is enabled, we don't change the value
            val casedValue = if (searchSpecAnnotation.caseSensitiveFlag) {
                value.toString()
            } else {
                value.toString().lowercase(Locale.ROOT)
            }
            val casedField = if (searchSpecAnnotation.caseSensitiveFlag) {
                path[fieldName]
            } else {
                builder.lower(path[fieldName])
            }
            return when (ops) {
                SearchOperation.STARTS_WITH -> builder.like(casedField, "$casedValue%")
                SearchOperation.ENDS_WITH -> builder.like(casedField, "%$casedValue")
                SearchOperation.CONTAINS -> builder.like((casedField), "%$casedValue%")
                SearchOperation.DOESNT_START_WITH -> builder.notLike(casedField, "$casedValue%")
                SearchOperation.DOESNT_END_WITH -> builder.notLike(casedField, "%$casedValue")
                SearchOperation.DOESNT_CONTAIN -> builder.notLike(casedField, "%$casedValue%")
                SearchOperation.GREATER_THAN -> builder.greaterThan(casedField, casedValue)
                SearchOperation.GREATER_THAN_EQUALS -> builder.greaterThanOrEqualTo(casedField, casedValue)
                SearchOperation.LESS_THAN -> builder.lessThan(path[fieldName], casedValue)
                SearchOperation.LESS_THAN_EQUALS -> builder.lessThanOrEqualTo(path[fieldName], casedValue)
                else -> super.buildPredicate(builder, path, fieldName, ops, value)
            }
        }
        return super.buildPredicate(builder, path, fieldName, ops, value)
    }
}
