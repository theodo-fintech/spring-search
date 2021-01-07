package com.sipios.springsearch

import com.sipios.springsearch.grammar.QueryLexer
import com.sipios.springsearch.grammar.QueryParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.springframework.data.jpa.domain.Specification

/**
 * Class used to parse a search query string and create a specification
 */
class CriteriaParser<T> {

    private val visitor = QueryVisitorImpl<T>()

    /**
     * Lexer -> Parser -> Visitor are used to build the specification
     *
     * @param searchParam The search param
     * @return a specification matching the input
     */
    fun parse(searchParam: String): Specification<T> {
        val parser = getParser(searchParam)
        return visitor.visit(parser.input())
    }

    private fun getParser(queryString: String): QueryParser {
        val lexer = QueryLexer(CharStreams.fromString(queryString))
        val tokens = CommonTokenStream(lexer)
        return QueryParser(tokens)
    }
}
