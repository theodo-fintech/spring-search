package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import com.sipios.springsearch.anotation.SearchSpec
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
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

    fun parse(value: List<*>?, fieldClass: KClass<out Any>): Any? {
        return value?.map { parse(it.toString(), fieldClass) }
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
            SearchOperation.IN_ARRAY -> {
                val inClause: CriteriaBuilder.In<Any> = getInClause(builder, path, fieldName, value)
                inClause
            }

            SearchOperation.NOT_IN_ARRAY -> {
                val inClause: CriteriaBuilder.In<Any> = getInClause(builder, path, fieldName, value)
                builder.not(inClause)
            }

            SearchOperation.IS -> {
                if (value == SearchOperation.NULL) {
                    builder.isNull(path.get<Any>(fieldName))
                } else {
                    // we should not call parent method for collection fields
                    // so this makes no sense to search for EMPTY with a non-collection field
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported operation $ops $value for field $fieldName")
                }
            }
            SearchOperation.IS_NOT -> {
                if (value == SearchOperation.NULL) {
                    builder.isNotNull(path.get<Any>(fieldName))
                } else {
                    // we should not call parent method for collection fields
                    // so this makes no sense to search for NOT EMPTY with a non-collection field
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported operation $ops $value for field $fieldName")
                }
            }

            SearchOperation.EQUALS -> builder.equal(path.get<Any>(fieldName), value)
            SearchOperation.NOT_EQUALS -> builder.notEqual(path.get<Any>(fieldName), value)
            SearchOperation.STARTS_WITH -> builder.like(path[fieldName], "$value%")
            SearchOperation.ENDS_WITH -> builder.like(path[fieldName], "%$value")
            SearchOperation.CONTAINS -> builder.like((path.get<String>(fieldName).`as`(String::class.java)), "%$value%")
            SearchOperation.DOESNT_START_WITH -> builder.notLike(path[fieldName], "$value%")
            SearchOperation.DOESNT_END_WITH -> builder.notLike(path[fieldName], "%$value")
            SearchOperation.DOESNT_CONTAIN -> builder.notLike(
                (path.get<String>(fieldName).`as`(String::class.java)),
                "%$value%"
            )

            else -> null
        }
    }

    fun getInClause(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        value: Any?
    ): CriteriaBuilder.In<Any> {
        val inClause: CriteriaBuilder.In<Any> = builder.`in`(path.get(fieldName))
        val values = value as List<*>
        values.forEach { inClause.value(it) }
        return inClause
    }

    companion object {
        fun getStrategy(fieldClass: KClass<out Any>, searchSpecAnnotation: SearchSpec, isCollectionField: Boolean): ParsingStrategy {
            return when {
                isCollectionField -> CollectionStrategy()
                fieldClass == Boolean::class -> BooleanStrategy()
                fieldClass == Date::class -> DateStrategy()
                fieldClass == Double::class -> DoubleStrategy()
                fieldClass == Float::class -> FloatStrategy()
                fieldClass == Int::class -> IntStrategy()
                fieldClass == Long::class -> LongStrategy()
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
