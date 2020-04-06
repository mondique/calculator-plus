package com.example.calculator

import android.util.Log

class Parser {
    private var errors: MutableList<String> = mutableListOf()
    private var incompleteLhsOps: MutableList<IExpressionFactory> = mutableListOf()
    private lateinit var lexerView: LexerView

    fun parse(expression: String, hasToEnd: Boolean = true): IRValue {
        return parse(LexerView(Lexer(expression)), hasToEnd)
    }

    private class LexerView(private val lexer: Lexer) {
        private var curToken = lexer.readToken()
        private var nextToken = lexer.readToken()
        private var nextNextToken = lexer.readToken()

        fun peek(): Token = curToken
        fun peekNext(): Token = nextToken
        fun peekNextNext(): Token = nextNextToken

        fun readToken(): Token {
            val result: Token = curToken
            move()
            return result
        }

        fun move() {
            curToken = nextToken
            nextToken = nextNextToken
            nextNextToken = lexer.readToken()
        }
    }

    private fun parse(expressionText: LexerView, hasToEnd: Boolean): IRValue {
        reset(expressionText)
        return parse(hasToEnd)
    }

    private fun reset(expressionText: LexerView) {
        errors = mutableListOf()
        incompleteLhsOps = mutableListOf()
        lexerView = expressionText
    }

    private fun parse(hasToEnd: Boolean): IRValue {
        while (true) {
            val curToken: Token = lexerView.peek()
            if (curToken is Token.ClosingParenthesis
                || curToken is Token.EOF
                || curToken is Token.ArgsAmount
                || curToken is Token.Comma
                || curToken is Token.Operator && !curToken.canBeUnary()) {
                throw ParserError("expected an expression before $curToken")
            }
            if (curToken is Token.Operator) {
                parseUnaryOperator()
                continue
            }
            val expr: IExpression = parseMember()
            val nextToken: Token = lexerView.peek()
            if (nextToken is Token.Operator && nextToken.isAssign()) {
                val lvalueExpr: ILValue = toLValueOrNull(expr) ?: throw ParserError("expected lvalue before =")
                parseAssignOperator(lvalueExpr)
                continue
            }
            if (expr !is IRValue) {
                throw ParserError("expected = after lvalue")
            }
            if (nextToken is Token.Operator && nextToken.canBeBinary()) {
                parseBinaryOperator(expr)
                continue
            }
            if (!hasToEnd || nextToken is Token.EOF) {
                return completeByRhs(expr)
            }
            throw ParserError("expected end of input or a binary operator instead of $nextToken")
        }
    }

    private fun parseMember(): IRValue {
        var result: IRValue = if (lexerView.peek() is Token.OpeningParenthesis)
            parseExpressionInParentheses() else parsePrimitive()
        while (lexerView.peek() is Token.OpeningParenthesis) {
            result = parseCallTo(result)
        }
        return result
    }

    private fun parsePrimitive(): IRValue {
        val curToken: Token = lexerView.readToken()
        return when(curToken) {
            is Token.Identifier -> Variable(curToken.name)
            is Token.Integer -> Value.Number.Integer(curToken.value)
            is Token.FloatingPointNumber -> Value.Number.RealNumber(curToken.value)
            else -> null
        }!!
    }

    private fun parseCallTo(function: IRValue): IRValue {
        val openingParen: Token.OpeningParenthesis = lexerView.readToken() as Token.OpeningParenthesis
        val args: MutableList<IRValue> = mutableListOf()
        if (lexerView.peek() !is Token.ClosingParenthesis) {
            while (true) {
                val argument: IRValue = Parser().parse(lexerView, hasToEnd=false)
                args.add(argument)
                val comma: Token = lexerView.peek()
                if (comma !is Token.Comma) {
                    break
                }
                lexerView.move()
            }
        }
        parseClosingParenthesis(openingParen)
        return FunctionCall(function, args.toList())
    }

    private fun toLValueOrNull(expr: IExpression): ILValue? {
        if (expr is ILValue) {
            return expr
        }
        if (expr is Variable) {
            return VariableDeclaration(expr.name)
        }
        if (expr is FunctionCall) {
            val function: IRValue = expr.functionTree
            if (function !is Variable) {
                return null
            }
            val args: List<String> = toArgNamesOrNull(expr.args) ?: return null
            return FunctionDeclaration(function.name, args)
        }
        return null
    }

    private fun toArgNamesOrNull(args: List<IRValue>): List<String>? {
        val result: MutableList<String> = mutableListOf()
        for (expr in args) {
            if (expr !is Variable) {
                return null
            }
            result.add(expr.name)
        }
        return result.toList()
    }

    private fun parseExpressionInParentheses(): IRValue {
        val openingParen: Token.OpeningParenthesis = lexerView.readToken() as Token.OpeningParenthesis
        val result: IRValue = Parser().parse(lexerView, hasToEnd=false)
        parseClosingParenthesis(openingParen)
        return result
    }

    private fun parseClosingParenthesis(openingParen: Token.OpeningParenthesis) {
        val closingParen: Token = lexerView.readToken()
        if (closingParen !is Token.ClosingParenthesis) {
            throw ParserError("expected a closing parenthesis instead of $closingParen")
        }
        if (!closingParen.matches(openingParen)) {
            throw ParserError("mismatched parentheses: $openingParen and $closingParen")
        }
    }

    private fun parseUnaryOperator() {
        val oper: Token.Operator = lexerView.readToken() as Token.Operator
        if (lastIncompleteExprIsHigherPriorityThanUnary(oper)) {
            throw ParserError("unary operator with lower priority met: $oper")
        }
        incompleteLhsOps.add(UnaryExpressionFactory(oper))
    }

    private fun lastIncompleteExprIsHigherPriorityThanUnary(oper: Token.Operator): Boolean {
        val lastIncompleteExpr: IExpressionFactory? = incompleteLhsOps.lastOrNull()
        return lastIncompleteExpr != null
               && lastIncompleteExpr is IOperatorExpressionFactory
               && lastIncompleteExpr.isHigherPriorityThanRightHandUnary(oper)
    }

    private fun parseBinaryOperator(immediateLhs: IRValue) {
        val oper: Token.Operator = lexerView.readToken() as Token.Operator
        var realLhs: IRValue = immediateLhs
        while (lastIncompleteExprIsHigherPriorityThanBinary(oper)) {
            realLhs = incompleteLhsOps.last().complete(realLhs)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        incompleteLhsOps.add(BinaryExpressionFactory(realLhs, oper))
    }

    private fun lastIncompleteExprIsHigherPriorityThanBinary(oper: Token.Operator): Boolean {
        val lastIncompleteExpr: IExpressionFactory? = incompleteLhsOps.lastOrNull()
        return lastIncompleteExpr != null
               && lastIncompleteExpr is IOperatorExpressionFactory
               && lastIncompleteExpr.isHigherPriorityThanRightHandBinary(oper)
    }

    private fun parseAssignOperator(lhs: ILValue) {
        lexerView.readToken() // Token.Operator("=")
        if (incompleteLhsOps.isNotEmpty()
            && incompleteLhsOps.last() !is AssignmentFactory) {
            throw ParserError("attempted assignment to non-lvalue")
        }
        incompleteLhsOps.add(AssignmentFactory(lhs))
    }

    private fun completeByRhs(rhs: IRValue): IRValue {
        var result = rhs
        while (incompleteLhsOps.isNotEmpty()) {
            result = incompleteLhsOps.last().complete(result)
            incompleteLhsOps.removeAt(incompleteLhsOps.lastIndex)
        }
        return result
    }
}

