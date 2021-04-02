package com.example.project.utils

object ResultHelper {
    private var Result0: String? = null
    private var Result1: String? = null
    private var Result2: String? = null
    private var Score0: Float = 0.0f
    private var Score1: Float = 0.0f
    private var Score2: Float = 0.0f

    public fun setResult0(result0: String) {
        Result0 = result0
    }

    public fun getResult0(): String? {
        return Result0
    }

    public fun setResult1(result1: String) {
        Result1 = result1
    }

    public fun getResult1(): String? {
        return Result1
    }

    public fun setResult2(result2: String) {
        Result2 = result2
    }

    public fun getResult2(): String? {
        return Result2
    }

    public fun setScore0(score0: Float) {
        Score0 = score0
    }

    public fun getScore0(): String {
        return "$Score0"
    }

    public fun setScore1(score1: Float) {
        Score1 = score1
    }

    public fun getScore1(): String {
        return "$Score1"
    }

    public fun setScore2(score2: Float) {
        Score2 = score2
    }

    public fun getScore2(): String {
        return "$Score2"
    }

    public override fun toString(): String {
        return "$Result0   $Score0\n" +
                "$Result1   $Score1\n" +
                "$Result2   $Score2\n"
    }
}