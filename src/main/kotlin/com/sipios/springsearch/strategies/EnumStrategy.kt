package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import kotlin.reflect.KClass

class EnumStrategy : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.EQUALS -> builder.equal(path.get<Any>(fieldName), value)
            SearchOperation.NOT_EQUALS -> builder.notEqual(path.get<Any>(fieldName), value)
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return Class.forName(fieldClass.qualifiedName).getMethod("valueOf", String::class.java).invoke(null, value)
    }
}
