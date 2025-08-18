package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import java.io.Serializable

data class Condition(
    val attribute: String,
    val operator: Operator,
    val value: Any,
    val dataType: DataType
) : Serializable
