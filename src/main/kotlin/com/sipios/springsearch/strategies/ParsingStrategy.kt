package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import com.sipios.springsearch.anotation.SearchSpec
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.UUID
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface ParsingStrategy {
    /**
     * Method to parse the value specified to the corresponding strategy
     *
     * @param value Value used for the search
     * @param fieldClass Kotlin class of the referred field
     * @return Returns by default the value without any parsing
     */
    fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return value
    }

    /**
     * Method to build the predicate
     *
     * @param builder Criteria object to build on
     * @param path Current path for predicate
     * @param fieldName Name of the field to be searched
     * @param ops Search operation to use
     * @param value Value used for the search
     * @return Returns a Predicate instance or null if the operation was not found
     */
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
        fun getStrategy(fieldClass: KClass<out Any>, searchSpecAnnotation: SearchSpec): ParsingStrategy {
            return when {
                fieldClass == Boolean::class -> BooleanStrategy()
                fieldClass == Date::class -> DateStrategy()
                fieldClass == Double::class -> DoubleStrategy()
                fieldClass == Float::class -> FloatStrategy()
                fieldClass == Int::class -> IntStrategy()
                fieldClass.isSubclassOf(Enum::class) -> EnumStrategy()
                fieldClass == Duration::class -> DurationStrategy()
                fieldClass == LocalDate::class -> LocalDateStrategy()
                fieldClass == LocalTime::class -> LocalTimeStrategy()
                fieldClass == LocalDateTime::class -> LocalDateTimeStrategy()
                fieldClass == Instant::class -> InstantStrategy()
                fieldClass == UUID::class -> UUIDStrategy()
                else -> StringStrategy(searchSpecAnnotation)
            }
        }
    }
}
