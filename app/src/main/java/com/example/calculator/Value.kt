package com.example.calculator

class Value(
    val valType: Type,
    val doubleVal: Double?,
    val intVal: Int?,
    val nameVal: String?,
    val errorVal: String?
) {
    enum class Type {
        INT, DOUBLE, NAME, ERROR
    }

    constructor(rawVal: String) : this(
        getStringType(rawVal),
        rawVal.toDoubleOrNull(),
        rawVal.toIntOrNull(),
        rawVal,
        rawVal
    ) {}

    constructor(value: Int) : this(
        Type.INT,
        null,
        value,
        null,
        null
    )

    constructor(value: Double) : this(
        Type.DOUBLE,
        value,
        null,
        null,
        null
    )

    companion object {
        fun getStringType(string: String): Type {
            val intVal = string.toIntOrNull()
            if (intVal != null) {
                return Type.INT
            }
            val doubleVal = string.toDoubleOrNull()
            if (doubleVal != null) {
                return Type.DOUBLE
            }
            if (isValidName(string)) {
                return Type.NAME
            }
            return Type.ERROR
        }
    }

    fun isInt(): Boolean = valType == Type.INT
    fun isDouble(): Boolean = valType == Type.DOUBLE
    fun isName(): Boolean = valType == Type.NAME
    fun isError(): Boolean = valType == Type.ERROR

    override fun toString(): String = when(valType) {
        Type.INT -> intVal!!.toString()
        Type.DOUBLE -> doubleVal!!.toString()
        Type.NAME -> nameVal!!
        Type.ERROR -> errorVal!!
    }

    fun toName(): String = when(valType) {
        Type.NAME -> nameVal!!
        else -> {
            assert(false)
            ""
        }
    }

    fun toDouble(): Double = when(valType) {
        Type.INT -> intVal!!.toDouble()
        Type.DOUBLE -> doubleVal!!
        Type.NAME -> {
            assert(false)
            .0
        }
        Type.ERROR -> {
            assert(false)
            .0
        }
    }

    fun toInt(): Int? = when(valType) {
        Type.INT -> intVal
        Type.DOUBLE -> {
            assert(false)
            0
        }
        Type.NAME -> {
            assert(false)
            0
        }
        Type.ERROR -> {
            assert(false)
            0
        }
    }
}

private fun isValidName(variableName: String): Boolean {
    if (variableName.length == 0 ||
        !IsValidFirstNameChar(variableName[0]) ||
        variableName == "ERROR")
        return false
    for (c in variableName) {
        if (!isValidNameChar(c))
            return false
    }
    return true
}

private fun isValidNameChar(c: Char): Boolean {
    if (c == '_')
        return true
    if (0 <= c - '0' && '9' - c >= 0)
        return true
    if (0 <= c - 'a' && 'z' - c >= 0)
        return true
    if (0 <= c - 'A' && 'Z' - c >= 0)
        return true
    return false
}

private fun IsValidFirstNameChar(c: Char): Boolean {
    if (c == '_')
        return true
    if (0 <= c - 'a' && 'z' - c >= 0)
        return true
    if (0 <= c - 'A' && 'Z' - c >= 0)
        return true
    return false
}

