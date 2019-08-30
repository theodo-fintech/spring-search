package com.sipios.srql

import org.springframework.data.jpa.domain.Specification

import java.util.Collections
import java.util.Deque
import java.util.LinkedList
import java.util.function.Function

class SpecificationsBuilder<U> {
    private var postFixedExprStack: Deque<*>? = null

    fun withSearch(search: String): SpecificationsBuilder<U> {
        postFixedExprStack = parser.parse(search)

        return this
    }

    /**
     * This function expect a search string to have ben provided.
     * The search string has been transformed into a Expression Queue with the format: [OR, value>100, AND, value<1000, label:*MONO*]
     *
     * @return A list of specification used to filter the underlying object using JPA specifications
     */
    fun build(): Specification<U> {
        val specStack = LinkedList<Specification<U>>()

        // Reverse the preFixedExpressionStack to make it into a postFixedExprStack
        // Info on those stacks: https://www.geeksforgeeks.org/stack-set-4-evaluation-postfix-expression/
        Collections.reverse(postFixedExprStack as List<*>?)

        while (!postFixedExprStack!!.isEmpty()) {
            val mayBeOperand = postFixedExprStack!!.pop()

            if (mayBeOperand !is String) {
                // The element in the stack is a comparaison value and not a AND or OR operand
                specStack.push(SpecificationImpl(mayBeOperand as SearchCriteria))
            } else {
                // The element in the stack is either a AND or an OR
                // Get the two specification that are impacted
                val operand1 = specStack.pop()
                val operand2 = specStack.pop()

                // Push the resulting specification
                if (mayBeOperand.equals(SearchOperation.AND_OPERATOR))
                    specStack.push(Specification.where(operand1)
                            .and(operand2))
                else if (mayBeOperand.equals(SearchOperation.OR_OPERATOR))
                    specStack.push(Specification.where(operand1)
                            .or(operand2))
            }

        }

        // Return the constructed specfication
        return specStack.pop()

    }

    companion object {

        private val parser = CriteriaParser()
    }
}
