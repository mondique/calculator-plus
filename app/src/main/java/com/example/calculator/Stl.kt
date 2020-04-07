package com.example.calculator

import kotlin.math.*

val stlValues: MutableMap<String, CalculationResult> = mutableMapOf(
    "sin" to CalculationResult.Function.STLFunction("sin", 1, ::stlSin)
)

fun stlSin(v: List<CalculationResult>): CalculationResult {
    val x: CalculationResult = v[0]
    if (x is CalculationResult.Function) {
        throw CalculationError("built-in functions cannot be applied to other functions yet")
    }
    val a: CalculationResult.Value = x as CalculationResult.Value
    return CalculationResult.Value.RealNumber(sin(a.toRealNumber().value))
}

