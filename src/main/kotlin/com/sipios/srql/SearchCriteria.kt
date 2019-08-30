package com.sipios.srql

import java.net.URLDecoder

class SearchCriteria// Change EQUALS into ENDS_WITH, CONTAINS, STARTS_WITH based on the presence of * in the value
(key: String, operation: String, prefix: String?, value: String, suffix: String?) {
    var key: String = key
    var operation: SearchOperation? = null
    var value: String? = null

    init {
        var op = SearchOperation.getSimpleOperation(operation[0])
        if (op != null) {
            // Change EQUALS into ENDS_WITH, CONTAINS, STARTS_WITH based on the presence of * in the value
            val startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX)
            val endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX)
            if (op === SearchOperation.EQUALS && startWithAsterisk && endWithAsterisk) {
                op = SearchOperation.CONTAINS
            } else if (op === SearchOperation.EQUALS && startWithAsterisk) {
                op = SearchOperation.ENDS_WITH
            } else if (op === SearchOperation.EQUALS && endWithAsterisk) {
                op = SearchOperation.STARTS_WITH
            }
            // Change NOT_EQUALS into DOESNT_ENDS_WITH, DOESNT_CONTAINS, DOESNT_STARTS_WITH based on the presence of * in the value
            if (op === SearchOperation.NOT_EQUALS && startWithAsterisk && endWithAsterisk) {
                op = SearchOperation.DOESNT_CONTAIN
            } else if (op === SearchOperation.NOT_EQUALS && startWithAsterisk) {
                op = SearchOperation.DOESNT_END_WITH
            } else if (op === SearchOperation.NOT_EQUALS && endWithAsterisk) {
                op = SearchOperation.DOESNT_START_WITH
            }
        }
        this.operation = op
        this.value = URLDecoder.decode(value,"UTF-8")
    }
}
