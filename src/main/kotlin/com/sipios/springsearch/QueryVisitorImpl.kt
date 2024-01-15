package com.sipios.springsearch

import com.sipios.springsearch.anotation.SearchSpec
import com.sipios.springsearch.grammar.QueryBaseVisitor
import com.sipios.springsearch.grammar.QueryParser
import org.springframework.data.jpa.domain.Specification

class QueryVisitorImpl<T>(private val searchSpecAnnotation: SearchSpec) : QueryBaseVisitor<Specification<T>>() {
    private val valueRegExp = Regex(pattern = "^(\\*?)(.+?)(\\*?)$")
    override fun visitOpQuery(ctx: QueryParser.OpQueryContext): Specification<T> {
        val left = visit(ctx.left)
        val right = visit(ctx.right)

        return when (ctx.logicalOp.text) {
            "AND" -> left.and(right)
            "OR" -> left.or(right)
            else -> left.and(right)
        }
    }

    override fun visitPriorityQuery(ctx: QueryParser.PriorityQueryContext): Specification<T> {
        return visit(ctx.query())
    }

    override fun visitAtomQuery(ctx: QueryParser.AtomQueryContext): Specification<T> {
        return visit(ctx.criteria())
    }

    override fun visitInput(ctx: QueryParser.InputContext): Specification<T> {
        return visit(ctx.query())
    }

    override fun visitIsCriteria(ctx: QueryParser.IsCriteriaContext): Specification<T> {
        val key = ctx.key()!!.text
        val op = if (ctx.IS() != null) {
            SearchOperation.IS
        } else {
            SearchOperation.IS_NOT
        }
        val value = ctx.is_value!!.text
        val criteria = SearchCriteria(
            key,
            op,
            null,
            value,
            null
        )
        return SpecificationImpl(criteria, searchSpecAnnotation)
    }

    override fun visitEqArrayCriteria(ctx: QueryParser.EqArrayCriteriaContext): Specification<T> {
        val key = ctx.key()!!.text
        val op = if (ctx.eq_array_value().IN() != null) {
            SearchOperation.IN_ARRAY
        } else {
            SearchOperation.NOT_IN_ARRAY
        }
        val arr = ctx.eq_array_value().array()
        val arrayValues = arr.value()
        val valueAsList: List<String> =
            arrayValues.map { if (it.STRING() != null) clearString(it.text) else it.text }
        // there is no need for prefix and suffix (e.g. 'john*') in case of array value
        val criteria = SearchCriteria(
            key,
            op,
            null,
            valueAsList,
            null
        )
        return SpecificationImpl(criteria, searchSpecAnnotation)
    }

    override fun visitBetweenCriteria(ctx: QueryParser.BetweenCriteriaContext): Specification<T> {
        val key = ctx.key()!!.text
        var leftValue = ctx.left!!.text
        var rightValue = ctx.right!!.text
        if (ctx.left!!.STRING() != null) {
            leftValue = clearString(leftValue)
        }
        if (ctx.right!!.STRING() != null) {
            rightValue = clearString(rightValue)
        }
        val opLeft = SearchOperation.GREATER_THAN_EQUALS
        val opRight = SearchOperation.LESS_THAN_EQUALS
        val criteriaLeft = SearchCriteria(
            key,
            opLeft,
            null,
            leftValue,
            null
        )
        val leftExp = SpecificationImpl<T>(criteriaLeft, searchSpecAnnotation)

        val criteriaRight = SearchCriteria(
            key,
            opRight,
            null,
            rightValue,
            null
        )

        val rightExp = SpecificationImpl<T>(criteriaRight, searchSpecAnnotation)

        return if (ctx.BETWEEN() != null) {
            leftExp.and(rightExp)
        } else
            Specification.not(leftExp.and(rightExp))
    }
    override fun visitOpCriteria(ctx: QueryParser.OpCriteriaContext): Specification<T> {
        val key = ctx.key()!!.text
        var value = ctx.value()!!.text
        if (ctx.value().STRING() != null) {
            value = clearString(value)
        }
        val matchResult = this.valueRegExp.find(value!!)
        val op = SearchOperation.getSimpleOperation(ctx.op().text)!!
        val criteria = SearchCriteria(
            key,
            op,
            matchResult!!.groups[1]!!.value,
            matchResult.groups[2]!!.value,
            matchResult.groups[3]!!.value
        )

        return SpecificationImpl(criteria, searchSpecAnnotation)
    }

    private fun clearString(value: String) = value
        .removeSurrounding("'")
        .removeSurrounding("\"")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
}
