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
        getVariableName(rawVal, 0, rawVal.length - 1),
        rawVal
    ) {}

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
            val nameVal = getVariableName(string, 0, string.length - 1)
            if (nameVal != null && isValidName(nameVal)) {
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

