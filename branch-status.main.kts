#!/usr/bin/env kotlin

@file:DependsOn("com.lordcodes.turtle:turtle:0.10.0")

import com.lordcodes.turtle.shellRun

val colours =
    mapOf(
        "reset" to "\u001B[0m",
        "bold" to "\u001B[1m",
        "green" to "\u001B[32m",
        "yellow" to "\u001B[33m",
        "cyan" to "\u001B[36m",
        "white" to "\u001B[37m",
    )

fun String.colour(colour: String) = "${colours["bold"]}${colours[colour]}$this${colours["reset"]}"

fun String.visibleLength(): Int {
    val ansiPattern = "\u001B\\[\\d+m".toRegex()
    return this.replace(ansiPattern, "").length
}

fun String.colourPadStart(
    length: Int,
    padChar: Char = ' ',
): String {
    val visibleLength = this.visibleLength()
    return if (visibleLength < length) {
        padChar.toString().repeat(length - visibleLength) + this
    } else {
        this
    }
}

fun String.colourPadEnd(
    length: Int,
    padChar: Char = ' ',
): String {
    val visibleLength = this.visibleLength()
    return if (visibleLength < length) {
        this + padChar.toString().repeat(length - visibleLength)
    } else {
        this
    }
}

val compareBranch = if (args.isEmpty()) "master" else args[0]

data class Branch(
    val name: String,
    val behind: Int,
    val ahead: Int,
)

val refsHeads = "refs/heads/"
val forEachRef = shellRun("git", listOf("for-each-ref", "--format='%(refname)'", refsHeads))
val branches = forEachRef.lines().map { it.drop(1).drop(refsHeads.length).dropLast(1) }
val longest = branches.maxBy { it.length }

val results =
    branches.map {
        val revList = shellRun("git", listOf("rev-list", "--left-right", "--count", "$compareBranch...$it"))
        val x = revList.lines()[0].split('\t')
        Branch(it, x[0].toInt(), x[1].toInt())
    }

val header = String.format("  | %s | Behind | Ahead |", "Branch".padEnd(longest.length))

fun line() = println("  ".padEnd(header.length, '-'))

if (args.isNotEmpty()) println("Comparing with branch '$compareBranch' ...")
line()
println(header)
line()
results.forEach {
    val name = it.name.colour("white")
    val behind = it.behind.toString().colour("cyan")
    val ahead = it.ahead.toString().colour("green")
    val c1 = name.colourPadEnd(longest.length)
    val c2 = behind.colourPadStart("Behind".length)
    val c3 = ahead.colourPadStart("Ahead".length)
    println("""  | $c1 | $c2 | $c3 |""")
}
line()
