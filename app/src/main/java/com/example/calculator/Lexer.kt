package com.example.calculator

import android.util.Log
import java.lang.Integer.min

class Lexer(private val source: String) {
    private var pos: Int = 0
    private var afterSpace: Boolean = false

    fun readToken(): Token {
        while (pos != source.length && source[pos] == ' ') {
            afterSpace = true
            pos++
        }
        if (pos == source.length) {
            return Token.EOF
        }
        if (onParenthesis()) {
            return getParenthesis()
        }
        if (onIdentifier()) {
            return getIdentifier()
        }
        if (onArgsAmount()) {
            return getArgsAmount()
        }
        if (onComma()) {
            return getComma()
        }
        if (onNumber()) {
            return getNumber()
        }
        val result: Token? = getOperatorOrNull()
        if (result != null) {
            return result
        }
        throw ParserError("invalid symbol: ${source[pos].toString()}")
    }

    private fun onParenthesis(): Boolean =
        source[pos] == '(' || source[pos] == ')'
        || source[pos] == '[' || source[pos] == ']'
        || source[pos] == '{' || source[pos] == '}'

    private fun getParenthesis(): Token {
        if (source[pos] == '(' || source[pos] == '[' || source[pos] == '{') {
            val result = Token.OpeningParenthesis(source[pos], source[pos].toString())
            pos++
            return result
        }
        val emacsBs = "}])"
        val result = Token.ClosingParenthesis(source[pos], source[pos].toString())
        pos++
        return result
    }

    private fun onIdentifier(): Boolean {
        val c = source[pos]
        if (c == '_')
            return true
        if (0 <= c - 'a' && 'z' - c >= 0)
            return true
        if (0 <= c - 'A' && 'Z' - c >= 0)
            return true
        return false
    }

    private fun getIdentifier(): Token {
        var nameEnd: Int = pos
        while (nameEnd != source.length && isValidIdentifierChar(source[nameEnd])) {
            nameEnd++
        }
        val resultSource: String = source.subSequence(pos, nameEnd).toString()
        val result = Token.Identifier(resultSource, resultSource)
        pos = nameEnd
        return result
    }

    private fun isValidIdentifierChar(c: Char): Boolean {
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

    private fun onArgsAmount(): Boolean = source[pos] == '$'

    private fun getArgsAmount(): Token {
        val prevPos: Int = pos
        pos++
        if (pos == source.length || !onInteger()) {
            throw ParserError("expected an integer after $")
        }
        return Token.ArgsAmount(getInteger(), source.subSequence(prevPos, pos).toString())
    }

    private fun onInteger(): Boolean = isDigit(source[pos])

    private fun getInteger(): Int {
        var numberEnd: Int = pos
        while (numberEnd != source.length && isValidIntegerChar(source[numberEnd])) {
            numberEnd++
        }
        val resultString: String = source.subSequence(pos, numberEnd).toString()
        pos = numberEnd
        return resultString.toInt()
    }

    private fun isValidIntegerChar(c: Char): Boolean = isDigit(c)

    private fun onComma(): Boolean = source[pos] == ','

    private fun getComma(): Token {
        val result: Token = Token.Comma(source[pos].toString())
        pos++
        return result
    }

    private fun onNumber(): Boolean {
        val c = source[pos]
        return c == '.' || isDigit(c)
    }

    private fun getNumber(): Token {
        var numberEnd: Int = pos
        while (numberEnd != source.length && isValidNumberChar(source[numberEnd])) {
            numberEnd++
        }
        val resultString: String = source.subSequence(pos, numberEnd).toString()
        pos = numberEnd
        val resultInt: Int? = resultString.toIntOrNull()
        if (resultInt != null) {
            return Token.Integer(resultInt, resultString)
        }
        val resultFloat: Double? = resultString.toDoubleOrNull()
        if (resultFloat != null) {
            return Token.FloatingPointNumber(resultFloat, resultString)
        }
        throw ParserError("invalid number: $resultString")
    }

    private fun isValidNumberChar(c: Char): Boolean =
        isDigit(c) || c == '.' || c == 'e' || c == 'E'

    private fun getOperatorOrNull(): Token? {
        var length: Int = min(3, source.length - pos)
        while (length > 0) {
            val resultSource: String = source.subSequence(pos, pos + length).toString()
            val result = Token.Operator(resultSource, resultSource)
            if (result.canBeUnary() || result.canBeBinary() || result.isAssign()) {
                pos += length
                return result
            }
            length--
        }
        return null
    }

    private fun isDigit(c: Char): Boolean =
        0 <= c - '0' && '9' - c >= 0
}

