package com.platform.groundcontrol.domain.enums

enum class Operator {
    EQUALS,           // ==
    NOT_EQUALS,       // !=
    GREATER_THAN,     // >
    GREATER_EQUAL,    // >=
    LESS_THAN,        // <
    LESS_EQUAL,       // <=
    IN,              // value in list
    NOT_IN,          // value not in list
    CONTAINS,        // string contains
    STARTS_WITH,     // string starts with
    ENDS_WITH,       // string ends with
    REGEX_MATCH      // regex pattern

}