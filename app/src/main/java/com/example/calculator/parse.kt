package com.example.calculator

import android.util.Log

fun parseString(text: String, scope: VariableScope): ExpressionNode? {
    return parseSubstring(text, 0, text.length - 1, scope)
}

fun parseSubstring(
    text: String,
    begin: Int,
    end: Int,
    scope: VariableScope
): ExpressionNode? {

    if (begin > end)
        return null

    if (!isValidBracketSequence(text, begin, end))
        return ExpressionNode(Value("ERROR: invalid bracket placement"))

    if (text[begin] == ' ')
        return parseSubstring(text, begin + 1, end, scope)
    if (text[end] == ' ')
        return parseSubstring(text, begin, end - 1, scope)

    if (canDiscardMarginalBrackets(text, begin, end))
        return parseSubstring(text, begin + 1, end - 1, scope)

    val substring = text.subSequence(begin, end + 1).toString()
    if (substring.toDoubleOrNull() != null)
        return ExpressionNode(Value(substring))

    val assignIndex = findLeftmostSymbolOnLevelZero(text, begin, end, "=")
    if (assignIndex != null) {
        val variableName = getVariableName(text, begin, assignIndex - 1)
        val rhs = parseSubstring(text, assignIndex + 1, end, scope)
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (variableName == null)
            return ExpressionNode(Value("ERROR: enter variable name"))
        if (!isValidName(variableName))
            return ExpressionNode(Value("ERROR: invalid variable name: $variableName"))
        return ExpressionNode(
            false,
            Operation(
                Operation.getType(text[assignIndex])!!),
            ExpressionNode(Value(variableName)),
            rhs,
            null
        )
    }

    val addSubstractIndex = findRightmostSymbolOnLevelZero(text, begin, end, "+-")
    if (addSubstractIndex != null && addSubstractIndex != begin) {
        val lhs = parseSubstring(text, begin, addSubstractIndex - 1, scope)
        val rhs = parseSubstring(text, addSubstractIndex + 1, end, scope)
        if (lhs == null)
            return ExpressionNode(Value("ERROR: no LHS"))
        Log.e(TAG, "LHS is not null")
        if (lhs.isVal() && lhs.getVal().isError())
            return lhs
        Log.e(TAG, "LHS is not error")
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (rhs.isVal() && rhs.getVal().isError())
            return rhs
        Log.e(TAG, "LHS and RHS OK")
        return ExpressionNode(
            false,
            Operation(Operation.getType(
                text[addSubstractIndex])!!), lhs, rhs,
            null
        )
    }

    val multiplyDivideIndex = findRightmostSymbolOnLevelZero(text, begin, end, "*/:")
    if (multiplyDivideIndex != null) {
        val lhs = parseSubstring(text, begin, multiplyDivideIndex - 1, scope)
        val rhs = parseSubstring(text, multiplyDivideIndex + 1, end, scope)
        if (lhs == null)
            return ExpressionNode(Value("ERROR: no LHS"))
        if (lhs.isVal() && lhs.getVal().isError())
            return lhs
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (rhs.isVal() && rhs.getVal().isError())
            return rhs
        return ExpressionNode(
            false,
            Operation(Operation.getType(
                text[multiplyDivideIndex])!!), lhs, rhs,
            null
        )
    }

    if (text[begin] == '-') {
        val rhs = parseSubstring(text, begin + 1, end, scope)
        if (rhs == null)
            return ExpressionNode(Value("ERROR: no RHS"))
        if (rhs.isVal() && rhs.getVal().isError())
            return rhs
        return ExpressionNode(
            false,
            Operation(Operation.Type.REVERT),
            null,
            rhs,
            null
        )
    }

    val inputText = text.subSequence(begin, end + 1).toString()
    val value: Value
    if (Value.getStringType(inputText) == Value.Type.ERROR) {
        value = Value("ERROR: \"$inputText\" is not a valid expression")
    } else {
        value = Value(inputText)
    }
    return ExpressionNode(value)
}

fun getVariableName(text: String, begin: Int, end: Int): String? {
    if (begin > end)
        return null

    if (text[begin] == ' ')
        return getVariableName(text, begin + 1, end)
    if (text[end] == ' ')
        return getVariableName(text, begin, end - 1)

    if (canDiscardMarginalBrackets(text, begin, end))
        return getVariableName(text, begin + 1, end - 1)

    return text.subSequence(begin, end + 1).toString()
}

fun isValidName(variableName: String): Boolean {
    if (variableName.length == 0 ||
        !IsValidFirstNameChar(variableName[0]) ||
        variableName == "ERROR")
        return false
    for (c in variableName) {
        if (!isValidNameChar(c))
            return false
    }
    return true
}

fun isValidNameChar(c: Char): Boolean {
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

fun IsValidFirstNameChar(c: Char): Boolean {
    if (c == '_')
        return true
    if (0 <= c - 'a' && 'z' - c >= 0)
        return true
    if (0 <= c - 'A' && 'Z' - c >= 0)
        return true
    return false
}

fun isDigit(c: Char): Boolean =
    (c - '0') >= 0 && ('9' - c) >= 0

fun isSign(c: Char): Boolean =
    Operation.getType(c) != null

fun isValidBracketSequence(
    text: String, begin: Int, end: Int): Boolean {
    var bracketsBalance = 0
    var index = end
    while (index >= begin) {
        bracketsBalance += when (text[index]) {
            '(' -> -1
            ')' -> 1
            else -> 0
        }
        if (bracketsBalance < 0)
            return false
        index -= 1
    }
    if (bracketsBalance != 0)
        return false
    return true
}

fun canDiscardMarginalBrackets(
    text: String, begin: Int, end: Int): Boolean {
    if (text[begin] != '(' || text[end] != ')')
        return false
    var bracketsBalance = 0
    var index = end
    while (index >= begin) {
        bracketsBalance += when (text[index]) {
            '(' -> -1
            ')' -> 1
            else -> 0
        }
        if (index != begin && bracketsBalance == 0)
            return false
        index -= 1
    }
    return true
}

fun findLeftmostSymbolOnLevelZero(
    text: String, begin: Int, end: Int, charset: String): Int? {
    var bracketsBalance = 0
    var index = begin
    while (index <= end) { //>
        bracketsBalance += when (text[index]) {
            '(' -> 1
            ')' -> -1
            else -> 0
        }
        if (bracketsBalance == 0 &&
            text[index] in charset)
            break
        index += 1
    }
    if (index > end)
        return null
    return index
}

fun findRightmostSymbolOnLevelZero(
    text: String, begin: Int, end: Int, charset: String): Int? {
    var bracketsBalance = 0
    var index = end
    while (index >= begin) {
        bracketsBalance += when (text[index]) {
            '(' -> -1
            ')' -> 1
            else -> 0
        }
        if (bracketsBalance == 0 &&
            text[index] in charset)
            break
        index -= 1
    }
    if (index < begin) //>
        return null
    return index
}

