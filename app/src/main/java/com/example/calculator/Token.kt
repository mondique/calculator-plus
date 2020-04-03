package com.example.calculator

sealed class Token() {
    abstract val source: String

    override fun toString(): String = source

    class Operator(val type: String, override val source: String) : Token() {
        enum class Direction {
            LeftToRight,
            RightToLeft
        }

        fun getBinaryCalcDir(): Direction = when(type) {
            "**" -> Direction.RightToLeft
            else -> Direction.LeftToRight
        }

        fun getAssignCalcDir(): Direction = Direction.RightToLeft

        fun getUnaryCalcDir(): Direction = Direction.RightToLeft

        fun toUnaryOperator(): IUnaryOperator? = when(type) {
            "+" -> IUnaryOperator.Add
            "-" -> IUnaryOperator.Revert
            "~" -> IUnaryOperator.BitwiseRevert
            else -> null
        }

        fun toBinaryOperator(): IBinaryOperator? = when(type) {
            "^" -> IBinaryOperator.Xor
            "+" -> IBinaryOperator.Add
            "-" -> IBinaryOperator.Subtract
            "*" -> IBinaryOperator.Multiply
            "/" -> IBinaryOperator.Divide
            "%" -> IBinaryOperator.Mod
            "**" -> IBinaryOperator.Power
            else -> null
        }

        fun isAssign(): Boolean =
            getAssignPriority() != -1

        fun canBeBinary(): Boolean =
            getBinaryPriority() != -1

        fun canBeUnary(): Boolean =
            getUnaryPriority() != -1

        fun getAssignPriority(): Int = if (type == "=") 1 else -1

        // Any two binary and unary operators should have different priorities
        fun getBinaryPriority(): Int =
            when(type) {
                "^" -> 6
                "+" -> 9
                "-" -> 9
                "*" -> 10
                "/" -> 10
                "%" -> 10
                "**" -> 13
                else -> -1
            }

        fun getUnaryPriority(): Int =
            when(type) {
                "+" -> 11
                "-" -> 11
                "~" -> 12
                else -> -1
            }
    }
    class Identifier(val name: String, override val source: String) : Token()
    class Integer(val value: Int, override val source: String) : Token()
    class FloatingPointNumber(val value: Double, override val source: String) : Token()
    class OpeningParenthesis(val type: Char, override val source: String) : Token()
    class ClosingParenthesis(val type: Char, override val source: String) : Token() {
        fun matches(openingParen: OpeningParenthesis): Boolean = when(openingParen.type) {
            '(' -> type == ')'
            '[' -> type == ']'
            '{' -> type == '}'
            else -> false
        }
    }
    object EOF : Token() {
        override val source = "end of input"
    }
}

