package com.sipios.srql

enum class SearchOperation {
    EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, STARTS_WITH, ENDS_WITH, CONTAINS;


    companion object {

        val SIMPLE_OPERATION_SET = arrayOf(":", "!", ">", "<", "~")

        val OR_PREDICATE_FLAG = "'"

        val ZERO_OR_MORE_REGEX = "*"

        val OR_OPERATOR = "OR"

        val AND_OPERATOR = "AND"

        val LEFT_PARANTHESIS = "("

        val RIGHT_PARANTHESIS = ")"

        /**
         * Parse a string into an operation.
         *
         * @param input operation as string
         * @return The matching operation
         */
        fun getSimpleOperation(input: Char): SearchOperation? {
            when (input) {
                ':' -> return EQUALS
                '!' -> return NOT_EQUALS
                '>' -> return GREATER_THAN
                '<' -> return LESS_THAN
                else -> return null
            }
        }
    }
}
