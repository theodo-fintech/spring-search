package com.sipios.srql

import com.google.common.base.Joiner
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

import java.util.Arrays // may not be useful, arrays already exist in Kotlin
import java.util.Collections
import java.util.Deque
import java.util.HashMap
import java.util.LinkedList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Class used to parse a search query string and create an expression queue
 */
class CriteriaParser {

    private enum class Operator private constructor(internal val precedence: Int) {
        OR(1), AND(2)
    }

    /**
     * Split the search string on whitespaces and then parse each token and their structure to create
     * a prefix expression stack
     *
     * @param searchParam The search param
     * @return a stack of parsed comparison and operations
     */
    fun parse(searchParam: String): Deque<*> {

        val prefixOutputQueue = LinkedList<Any>()
        val stack = LinkedList<String>()
        searchParam.split(Regex("\\s+")).forEach { token -> parseToken(token, prefixOutputQueue, stack) }

        while (!stack.isEmpty())
            prefixOutputQueue.push(stack.pop())

        return prefixOutputQueue
    }

    private fun parseToken(token: String, output: Deque<Any>, stack: Deque<String>) {
        if (isLogicalOperator(token)) {
            while (!stack.isEmpty() && isHigherPrecedenceOperator(token, stack.peek())) {
                output.push(
                        if (stack.pop().equals(SearchOperation.OR_OPERATOR, true))
                            SearchOperation.OR_OPERATOR
                        else
                            SearchOperation.AND_OPERATOR
                )
            }
            stack.push(
                    if (token.equals(SearchOperation.OR_OPERATOR, true))
                        SearchOperation.OR_OPERATOR
                    else
                        SearchOperation.AND_OPERATOR)
        } else if (token.equals(SearchOperation.LEFT_PARANTHESIS)) {
            stack.push(SearchOperation.LEFT_PARANTHESIS)
        } else if (token.equals(SearchOperation.RIGHT_PARANTHESIS)) {
            finishParenthesisGroup(output, stack)
        } else {
            val matcher = SpecCriteraRegex.matcher(token)
            while (matcher.find()) {
                output.push(SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5)))
            }
        }
    }

    /**
     * When a ) as been detected, we assume that a corresponding ( was there before.
     * We build back the stack until either a ( or it is empty
     * If it is empty, no matchin ( was found to the ) and thus the input can be deemed incorrect
     *
     * @param output
     * @param stack
     */
    private fun finishParenthesisGroup(output: Deque<Any>, stack: Deque<String>) {
        while (!SearchOperation.LEFT_PARANTHESIS.equals(stack.peek())) {
            if (stack.peek() == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No Matching ( for the corresponding )")
            }
            output.push(stack.pop())
        }
        stack.pop()
    }

    companion object {

        private var operators: Map<String, Operator>? = null

        /**
         * Pattern to match an expression
         *
         * -- Examples --
         * property:value
         * property>value
         * parent.property:*value*
         * parent.property:*value
         * parent.property:value*
         */
        private val SpecCriteraRegex = Pattern.compile("^([\\w\\.]+?)(" + Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET) + ")(\\*?)([^\\p{Space}]+?)(\\*?)$")

        init {
            val tempMap = HashMap<String, Operator>()
            tempMap.put("AND", Operator.AND)
            tempMap.put("OR", Operator.OR)
            tempMap.put("or", Operator.OR)
            tempMap.put("and", Operator.AND)

            operators = Collections.unmodifiableMap(tempMap)
        }

        private fun isHigherPrecedenceOperator(currOp: String, prevOp: String): Boolean {
            return isLogicalOperator(prevOp) && (operators!![prevOp] ?: error("")).precedence >= (operators!![currOp] ?: error("")).precedence
        }

        /**
         *
         * @param token a token present in the search string. Can either be a comparison value or an Operator AND/OR
         * @return a boolean that is true if the token is an Operator
         */
        private fun isLogicalOperator(token: String): Boolean {
            return operators!!.containsKey(token)
        }
    }

}
