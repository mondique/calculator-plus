package com.example.calculator

import android.util.Log

class Parser(val errors: MutableList<String>) {
    private var incompleteLhsOps: MutableList<ExpressionFactory> = mutableListOf()
    private lateinit var lexerView: LexerView

    fun parse(expression: String, hasToEnd: Boolean = true): Expression? {
        return parse(LexerView(Lexer(expression)), hasToEnd)
    }

    private fun parse(expression: LexerView, hasToEnd: Boolean): Expression? {
        reset(expression)
        return parse(hasToEnd)
    }

    private class LexerView(private val lexer: Lexer) {
        private var curToken = lexer.getToken()

        fun peek(): Token = curToken

        fun getToken(): Token {
            val result: Token = curToken
            move()
            return result
        }

        fun move() {
            Log.e("TAG", "LexerView::Move")
            if (curToken != Token.EOF) {
                curToken = lexer.getToken()
            }
        }

        fun reachedEnd(): Boolean = curToken == Token.EOF
    }

    private fun reset(expression: LexerView) {
        incompleteLhsOps = mutableListOf()
        lexerView = expression
    }

    private fun parse(hasToEnd: Boolean): Expression? {
        while (!lexerView.reachedEnd()) {
            Log.e("TAG", "parse::while ${lexerView.peek()}")
            val lhs = parseMinExpression()
            val oper = parseOperator()
            Log.e("TAG", "parse::while after getting lhs=$lhs and oper=$oper")
            if (lhs == null) {
                if (oper == null) {
                    throwError("invalid symbol")
                    return null
                }
                if (!oper.canBeUnary()) {
                    throwError("no lhs for operator ${oper.type}")
                    return null
                }
                addUnaryOp(oper)
                continue
            }
            if (oper == null) {
                if (hasToEnd && !lexerView.reachedEnd()) {
                    throwError("expression ends prematurely")
                    return null
                }
                Log.e("TAG", "parse::while before completeLhsOps($lhs)")
                return completeLhsOps(lhs)
            }
            Log.e("TAG", "Before Parser::addLhsOp with lhs=$lhs, oper=$oper")
            addLhsOp(lhs, oper)
        }
        throwError("unexpected input end")
        return null
    }

    private fun parseMinExpression(): Expression? {
        val exprInParentheses: Expression? = parseExpressionInParentheses()
        if (exprInParentheses != null) {
            return exprInParentheses
        }
        val curToken: Token = lexerView.peek()
        val result: Expression? = when(curToken) {
            is Token.Identifier -> Expression(Value.Variable(curToken.name))
            is Token.Integer -> Expression(Value.Number.Integer(curToken.value))
            is Token.FloatingPointNumber -> Expression(Value.Number.RealNumber(curToken.value))
            else -> null
        }
        if (result != null) {
            lexerView.move()
        }
        return result
    }

    private fun parseExpressionInParentheses(): Expression? {
        val openingParen: Token = lexerView.peek()
        if (openingParen !is Token.Parenthesis || !openingParen.isOpening()) {
            return null
        }
        lexerView.move()
        val result: Expression? = Parser(errors).parse(lexerView, hasToEnd=false)
        val closingParen: Token = lexerView.peek()
        if (closingParen !is Token.Parenthesis || !closingParen.matches(openingParen)) {
            throwError("no matching parenthesis for ${openingParen.type}")
            return null
        }
        lexerView.move()
        if (result == null) {
            throwError("invalid (or empty) expression in parentheses")
        }
        return result
    }

    private fun parseOperator(): Token.Operator? {
        val result: Token = lexerView.peek()
        if (result is Token.Operator) {
            lexerView.move()
            return result
        }
        return null
    }

    private fun addUnaryOp(oper: Token.Operator) {
        if (!incompleteLhsOps.isEmpty() &&
            incompleteLhsOps.last().isHigherPriorityThanRightHandUnary(oper)) {
            throwError("unary operator with lower priority met: $oper")
            return
        }
        incompleteLhsOps.add(ExpressionFactory(oper))
    }

    private fun addLhsOp(lhs: Expression, oper: Token.Operator) {
        val realLhs = combineHigherPriorityLhsOps(lhs, oper)
        incompleteLhsOps.add(ExpressionFactory(realLhs, oper))
    }

    private fun combineHigherPriorityLhsOps(lhs: Expression, oper: Token.Operator): Expression {
        var finalLhs = lhs
        while (!incompleteLhsOps.isEmpty() &&
               incompleteLhsOps.last().isHigherPriorityThanRightHandBinary(oper)) {
            finalLhs = incompleteLhsOps.last().complete(finalLhs)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        return finalLhs
    }

    private fun completeLhsOps(rhs: Expression): Expression {
        var result = rhs
        while (!incompleteLhsOps.isEmpty()) {
            result = incompleteLhsOps.last().complete(result)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        return result
    }

    private fun throwError(error: String) {
        errors.add(error)
    }
}

