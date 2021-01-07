package com.sipios.springsearch

class SearchCriteria // Change EQUALS into ENDS_WITH, CONTAINS, STARTS_WITH based on the presence of * in the value
    (var key: String, operation: String, prefix: String?, var value: String, suffix: String?) {
    var operation: SearchOperation?

    init {
        var op = SearchOperation.getSimpleOperation(operation[0])
        if (op != null) {
            // Change EQUALS into ENDS_WITH, CONTAINS, STARTS_WITH based on the presence of * in the value
            val startsWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX)
            val endsWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX)
            if (op === SearchOperation.EQUALS && startsWithAsterisk && endsWithAsterisk) {
                op = SearchOperation.CONTAINS
            } else if (op === SearchOperation.EQUALS && startsWithAsterisk) {
                op = SearchOperation.ENDS_WITH
            } else if (op === SearchOperation.EQUALS && endsWithAsterisk) {
                op = SearchOperation.STARTS_WITH
            }
            // Change NOT_EQUALS into DOESNT_END_WITH, DOESNT_CONTAIN, DOESNT_START_WITH based on the presence of * in the value
            if (op === SearchOperation.NOT_EQUALS && startsWithAsterisk && endsWithAsterisk) {
                op = SearchOperation.DOESNT_CONTAIN
            } else if (op === SearchOperation.NOT_EQUALS && startsWithAsterisk) {
                op = SearchOperation.DOESNT_END_WITH
            } else if (op === SearchOperation.NOT_EQUALS && endsWithAsterisk) {
                op = SearchOperation.DOESNT_START_WITH
            }
        }
        this.operation = op
    }
}
