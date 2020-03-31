package com.example.calculator

class ExpressionFactory(val lhs: IExpression?, val oper: Token.Operator) {
    constructor(oper: Token.Operator) : this(null, oper) {}

    fun complete(rhs: IExpression): IExpression {
        if (isUnaryOperator()) {
            return UnaryExpression(oper.toUnaryOperator()!!, rhs)
        }
        return BinaryExpression(oper.toBinaryOperator()!!, lhs!!, rhs)
    }

    fun isHigherPriorityThanRightHandUnary(otherOper: Token.Operator): Boolean =
        getOperatorPriority() > otherOper.getUnaryPriority()
        || getOperatorPriority() == otherOper.getUnaryPriority()
        && otherOper.getUnaryCalcDir() == Token.Operator.Direction.LeftToRight

    fun isHigherPriorityThanRightHandBinary(otherOper: Token.Operator): Boolean =
        getOperatorPriority() > otherOper.getBinaryPriority()
        || getOperatorPriority() == otherOper.getBinaryPriority()
        && otherOper.getBinaryCalcDir() == Token.Operator.Direction.LeftToRight

    private fun getOperatorPriority(): Int =
        if (isUnaryOperator())
            oper.getUnaryPriority()
        else
            oper.getBinaryPriority()

    private fun isUnaryOperator(): Boolean = lhs == null
}

