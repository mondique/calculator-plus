package com.example.calculator

import android.util.Log

class Parser() {
    private var errors: MutableList<String> = mutableListOf()
    private var incompleteLhsOps: MutableList<ExpressionFactory> = mutableListOf()
    private lateinit var lexerView: LexerView

    fun parse(expression: String, hasToEnd: Boolean = true): IExpression {
        return parse(LexerView(Lexer(expression)), hasToEnd)
    }

    private class LexerView(private val lexer: Lexer) {
        private var curToken = lexer.readToken()

        fun peek(): Token = curToken

        fun readToken(): Token {
            val result: Token = curToken
            move()
            return result
        }

        fun move() {
            if (curToken !is Token.EOF) {
                curToken = lexer.readToken()
            }
        }

        fun reachedEnd(): Boolean = curToken is Token.EOF
    }

    private fun parse(expressionText: LexerView, hasToEnd: Boolean): IExpression {
        reset(expressionText)
        return parse(hasToEnd)
    }

    private fun reset(expressionText: LexerView) {
        errors = mutableListOf()
        incompleteLhsOps = mutableListOf()
        lexerView = expressionText
    }

    private fun parse(hasToEnd: Boolean): IExpression {
        while (true) {
            val curToken: Token = lexerView.peek()
            if (curToken is Token.ClosingParenthesis
                || curToken is Token.EOF
                || curToken is Token.Operator && !curToken.canBeUnary()) {
                throw ParserError("expected an expression before $curToken")
            }
            if (curToken is Token.Operator) {
                parseUnaryOperator()
                continue
            }
            val expr: IExpression = if (curToken is Token.OpeningParenthesis) {
                parseExpressionInParentheses()
            } else {
                parsePrimitive()
            }
            val nextToken: Token = lexerView.peek()
            if (nextToken is Token.Operator && nextToken.canBeBinary()) {
                parseBinaryOperator(expr)
                continue
            }
            if (nextToken is Token.Operator && nextToken.isAssign()) {
                parseAssignOperator(expr)
                continue
            }
            if (!hasToEnd || nextToken is Token.EOF) {
                return completeByRhs(expr)
            }
            throw ParserError("expected end of input or a binary operator instead of $nextToken")
        }
    }

    private fun parsePrimitive(): IExpression {
        val curToken: Token = lexerView.readToken()
        val result: IExpression? = when(curToken) {
            is Token.Identifier -> Value.Variable(curToken.name)
            is Token.Integer -> Value.Number.Integer(curToken.value)
            is Token.FloatingPointNumber -> Value.Number.RealNumber(curToken.value)
            else -> null
        }
        return result!!
    }

    private fun parseExpressionInParentheses(): IExpression {
        val openingParen: Token.OpeningParenthesis = lexerView.readToken() as Token.OpeningParenthesis
        val result: IExpression = Parser().parse(lexerView, hasToEnd=false)
        val closingParen: Token = lexerView.readToken()
        if (closingParen !is Token.ClosingParenthesis
            || !closingParen.matches(openingParen)) {
            throw ParserError("mismatched parentheses: $openingParen and $closingParen")
        }
        return result
    }

    private fun parseUnaryOperator() {
        val oper: Token.Operator = lexerView.readToken() as Token.Operator
        if (!incompleteLhsOps.isEmpty() &&
            incompleteLhsOps.last().isHigherPriorityThanRightHandUnary(oper)) {
            throw ParserError("unary operator with lower priority met: $oper")
            return
        }
        incompleteLhsOps.add(ExpressionFactory(oper))
    }

    private fun parseBinaryOperator(immediateLhs: IExpression) {
        val oper: Token.Operator = lexerView.readToken() as Token.Operator
        var realLhs = immediateLhs
        while (!incompleteLhsOps.isEmpty()
               && incompleteLhsOps.last().isHigherPriorityThanRightHandBinary(oper)) {
            realLhs = incompleteLhsOps.last().complete(realLhs)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        incompleteLhsOps.add(ExpressionFactory(realLhs, oper))
    }

    private fun parseAssignOperator(immediateLhs: IExpression) {
        val oper: Token.Operator = lexerView.readToken() as Token.Operator
        var realLhs = immediateLhs
        while (!incompleteLhsOps.isEmpty()
               && incompleteLhsOps.last().isHigherPriorityThanRightHandAssign(oper)) {
            realLhs = incompleteLhsOps.last().complete(realLhs)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        if (realLhs !is Value.Variable) {
            throw ParserError("attempted assignment to a non-variable")
        }
        incompleteLhsOps.add(ExpressionFactory(realLhs as Value.Variable, oper))
    }

    private fun completeByRhs(rhs: IExpression): IExpression {
        var result = rhs
        while (!incompleteLhsOps.isEmpty()) {
            result = incompleteLhsOps.last().complete(result)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        return result
    }
}

