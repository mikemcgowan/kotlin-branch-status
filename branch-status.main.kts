#!/usr/bin/env kotlin

@file:DependsOn("com.lordcodes.turtle:turtle:0.6.0")

import com.lordcodes.turtle.shellRun

data class Branch(val name: String, val behind: Int, val ahead: Int)

val refsHeads = "refs/heads/"
val forEachRef = shellRun("git", listOf("for-each-ref", "--format='%(refname)'", refsHeads))
val branches = forEachRef.lines().map { it.drop(1).drop(refsHeads.length).dropLast(1) }
val longest = branches.maxBy { it.length }

val results = branches.map {
    val revList = shellRun("git", listOf("rev-list", "--left-right", "--count", "master...$it"))
    val x = revList.lines()[0].split('\t')
    Branch(it, x[0].toInt(), x[1].toInt())
}

fun line() = println(String.format("  %s", "".padEnd(longest.length + 18, '-')))

line()
results.forEach { println(String.format("  | %s | %4s | %4s |", it.name.padEnd(longest.length), it.behind, it.ahead)) }
line()
