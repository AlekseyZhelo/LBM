package com.alekseyzhelo.lbm.util

import java.text.DecimalFormat

/**
 * @author Aleks on 28-05-2016.
 */

@Suppress("NOTHING_TO_INLINE") // TODO: investigate
inline fun normalize(value: Double, minValue: Double, maxValue: Double): Double {
    return (value - minValue) / (maxValue - minValue)
}

fun norm(U: DoubleArray): Double {
    var norm = 0.0;
    for (i in U.indices) {
        norm += U[i] * U[i];
    }
    return Math.sqrt(norm);
}

fun normSquare(U: DoubleArray): Double {
    var uSqr = 0.0;
    for (i in U.indices) {
        uSqr += U[i] * U[i];
    }
    return uSqr;
}

fun scalarProduct(U: DoubleArray, V: DoubleArray): Double {
    var prod = 0.0;
    for (i in U.indices) {
        prod += U[i] * V[i];
    }
    return prod;
}

private val doubleFormat = DecimalFormat("+0.00;-0.00")
fun Double.format() = doubleFormat.format(this)
fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)