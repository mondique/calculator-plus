package com.example.calculator

sealed class Token {
    data class Operator(val type: String) : Token() {
        enum class Direction {
            LeftToRight,
            RightToLeft
        }

        fun getBinaryCalcDir(): Direction = when(type) {
            "=" -> Direction.RightToLeft
            "**" -> Direction.RightToLeft
            else -> Direction.LeftToRight
        }

        fun getUnaryCalcDir(): Direction = Direction.RightToLeft

        fun toUnaryOperator(): IUnaryOperator? = when(type) {
            "+" -> IUnaryOperator.Add()
            "-" -> IUnaryOperator.Revert()
            "~" -> IUnaryOperator.BitwiseRevert()
            else -> null
        }

        fun toBinaryOperator(): IBinaryOperator? = when(type) {
            "^" -> IBinaryOperator.Xor()
            "+" -> IBinaryOperator.Add()
            "-" -> IBinaryOperator.Subtract()
            "*" -> IBinaryOperator.Multiply()
            "/" -> IBinaryOperator.Divide()
            "%" -> IBinaryOperator.Mod()
            "**" -> IBinaryOperator.Power()
            "=" -> IBinaryOperator.Assign()
            else -> null
        }

        fun canBeBinary(): Boolean =
            getBinaryPriority() != -1

        fun canBeUnary(): Boolean =
            getUnaryPriority() != -1

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
                "=" -> 18
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
    data class Identifier(val name: String) : Token()
    data class Integer(val value: Int) : Token()
    data class FloatingPointNumber(val value: Double) : Token()
    data class Parenthesis(val type: Char) : Token() {
        fun isOpening(): Boolean = type == '('

        fun matches(openingParen: Parenthesis): Boolean =
            openingParen.isOpening() && type == ')'
    }
    object EOF : Token()
    object Error : Token()
}

