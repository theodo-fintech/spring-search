package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import java.util.Date
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import kotlin.reflect.KClass

interface ParsingStrategy {
    fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return value
    }

    fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.EQUALS -> builder.equal(path.get<Any>(fieldName), value)
            SearchOperation.NOT_EQUALS -> builder.notEqual(path.get<Any>(fieldName), value)
            SearchOperation.STARTS_WITH -> builder.like(path.get(fieldName), "$value%")
            SearchOperation.ENDS_WITH -> builder.like(path.get(fieldName), "%$value")
            SearchOperation.CONTAINS -> builder.like((path.get<String>(fieldName).`as`(String::class.java)), "%$value%")
            SearchOperation.DOESNT_START_WITH -> builder.notLike(path.get(fieldName), "$value%")
            SearchOperation.DOESNT_END_WITH -> builder.notLike(path.get(fieldName), "%$value")
            SearchOperation.DOESNT_CONTAIN -> builder.notLike(
                (path.get<String>(fieldName).`as`(String::class.java)),
                "%$value%"
            )
            else -> null
        }
    }

    companion object {
        fun getStrategy(fieldClass: KClass<out Any>): ParsingStrategy {
            return when (fieldClass) {
                Boolean::class -> BooleanStrategy()
                Double::class -> DoubleStrategy()
                Float::class -> FloatStrategy()
                Int::class -> IntStrategy()
                Date::class -> DateStrategy()
                else -> StringStrategy()
            }
        }
    }
}
