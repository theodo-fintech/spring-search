package com.sipios.springsearch

enum class SearchOperation {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS,
    DOESNT_START_WITH,
    DOESNT_END_WITH,
    DOESNT_CONTAIN,
    GREATER_THAN_EQUALS,
    LESS_THAN_EQUALS,
    EQUALS_ARRAY,
    NOT_EQUALS_ARRAY
    ;

    companion object {
        val SIMPLE_OPERATION_SET = arrayOf(":", "!", ">", "<", "~", ">:", "<:")
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
        fun getSimpleOperation(
            input: String,
            valueIsArray: Boolean
        ): SearchOperation? {
            return when (input) {
                ":" -> if (valueIsArray) EQUALS_ARRAY else EQUALS
                "!" -> if (valueIsArray) NOT_EQUALS_ARRAY else NOT_EQUALS
                ">" -> GREATER_THAN
                "<" -> LESS_THAN
                ">:" -> GREATER_THAN_EQUALS
                "<:" -> LESS_THAN_EQUALS
                else -> null
            }
        }
    }
}
