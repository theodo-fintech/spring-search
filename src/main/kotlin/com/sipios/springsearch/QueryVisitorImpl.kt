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

    override fun visitCriteria(ctx: QueryParser.CriteriaContext): Specification<T> {
        val key = ctx.key()!!.text
        val op = ctx.op()!!.text
        var value = ctx.value()!!.text
        var valueAsList: List<String>? = null
        if (ctx.value().STRING() != null) {
            value = clearString(value)
        } else if (ctx.value().array() != null) {
            val arr = ctx.value().array()
            val arrayValues = arr.value()
            valueAsList = arrayValues.map({if (it.STRING() != null) clearString(it.text) else it.text})
        }

        val matchResult = this.valueRegExp.find(value!!)
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
