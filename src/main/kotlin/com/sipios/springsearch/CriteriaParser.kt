package com.sipios.springsearch

import SyntaxErrorListener
import com.sipios.springsearch.anotation.SearchSpec
import com.sipios.springsearch.grammar.QueryLexer
import com.sipios.springsearch.grammar.QueryParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Class used to parse a search query string and create a specification
 */
class CriteriaParser<T>(searchSpecAnnotation: SearchSpec) {

    private val visitor = QueryVisitorImpl<T>(searchSpecAnnotation)

    /**
     * Lexer -> Parser -> Visitor are used to build the specification
     *
     * @param searchParam The search param
     * @return a specification matching the input
     */
    fun parse(searchParam: String): Specification<T> {
        val parser = getParser(searchParam)
        val listener = SyntaxErrorListener()
        parser.addErrorListener(listener)
        // complete parse tree before visiting
        val input = parser.input()
        if (parser.numberOfSyntaxErrors > 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search query: $listener")
        }
        return visitor.visit(input)
    }

    private fun getParser(queryString: String): QueryParser {
        val lexer = QueryLexer(CharStreams.fromString(queryString))
        val tokens = CommonTokenStream(lexer)
        return QueryParser(tokens)
    }
}
