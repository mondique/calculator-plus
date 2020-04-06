package com.example.calculator

interface IExpressionFactory {
    fun complete(rhs: IRValue): IRValue
}

interface IOperatorExpressionFactory: IExpressionFactory {
    fun isHigherPriorityThanRightHandUnary(otherOper: Token.Operator): Boolean =
        getOperatorPriority() > otherOper.getUnaryPriority()
        || getOperatorPriority() == otherOper.getUnaryPriority()
        && otherOper.getUnaryCalcDir() == Token.Operator.Direction.LeftToRight

    fun isHigherPriorityThanRightHandBinary(otherOper: Token.Operator): Boolean =
        getOperatorPriority() > otherOper.getBinaryPriority()
        || getOperatorPriority() == otherOper.getBinaryPriority()
        && otherOper.getBinaryCalcDir() == Token.Operator.Direction.LeftToRight

    fun getOperatorPriority(): Int
}

class UnaryExpressionFactory(val oper: Token.Operator) : IOperatorExpressionFactory {
    override fun complete(rhs: IRValue): IRValue = UnaryExpression(oper.toUnaryOperator()!!, rhs)

    override fun getOperatorPriority(): Int = oper.getUnaryPriority()
}

class BinaryExpressionFactory(val lhs: IRValue, val oper: Token.Operator) : IOperatorExpressionFactory {
    override fun complete(rhs: IRValue): IRValue = BinaryExpression(oper.toBinaryOperator()!!, lhs, rhs)

    override fun getOperatorPriority(): Int = oper.getBinaryPriority()
}

class AssignmentFactory(val lhs: ILValue) : IExpressionFactory {
    override fun complete(rhs: IRValue): IRValue = Assignment(lhs, rhs)
}

