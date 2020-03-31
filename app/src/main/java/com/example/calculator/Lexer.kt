package com.example.calculator

import android.util.Log
import java.lang.Integer.min

class Lexer(private val source: String) {
    private var pos: Int = 0
    private var afterSpace: Boolean = false

    fun getToken(): Token {
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
        if (onNumber()) {
            return getNumber()
        }
        return getOperatorOrNull() ?: Token.Error
    }

    private fun onParenthesis(): Boolean =
        source[pos] == '(' || source[pos] == ')'

    private fun getParenthesis(): Token {
        val result = Token.Parenthesis(source[pos])
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
        val result = Token.Identifier(source.subSequence(pos, nameEnd).toString())
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
            return Token.Integer(resultInt)
        }
        val resultFloat: Double? = resultString.toDoubleOrNull()
        if (resultFloat != null) {
            return Token.FloatingPointNumber(resultFloat)
        }
        return Token.Error
    }

    private fun isValidNumberChar(c: Char): Boolean =
        isDigit(c) || c == '.' || c == 'e' || c == 'E'

    private fun getOperatorOrNull(): Token? {
        var length: Int = min(3, source.length - pos)
        while (length > 0) {
            val result = Token.Operator(source.subSequence(pos, pos + length).toString())
            if (result.canBeUnary() || result.canBeBinary()) {
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

