package com.lightspeed.interview.ipv4

import org.springframework.util.ResourceUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*


fun main() {
    val file = ResourceUtils.getFile("classpath:ipv4.txt")
    val uniqueIpv4Count = findUniqueIpv4Count(file)
    println("RESULT: $uniqueIpv4Count ipv4 found")
}

// The algorithm: 1) read each line, 2) map it to a unique long value, 3) use two BitSet to check for uniqueness
// Why it works: The number of unique ipv4 addresses is 4,294,967,296, therefore we can map each one to a unique number in range 0 to 4294967295
// Memory Usage: Each of the two BitSets will have just enough capacity for storing all non-negative int values, which is 256MB. Overall: 512MB
// Naive Solution Memory Usage: Well over tens of gigabytes
fun findUniqueIpv4Count(file: File): Long {
    val inputStream = FileInputStream(file)
    val bufferedInputStreamReader = BufferedInputStream(inputStream).reader()

    // BitSet for checking long values that can be turned to positive integers: 1 to 2147483647
    val smallerValuesBitSet = BitSet(Int.MAX_VALUE)
    // BitSet for checking long values that are larger than int max value: 2147483648 to 4294967295
    val largerValuesBitSet = BitSet()

    // since 0 is out of range for the bitsets, will have to use separate variables
    var lowZeroCount = 0
    var highZeroCount = 0

    bufferedInputStreamReader.forEachLine { line ->
        val ipv4Long = line.ipv4ToLong()
        println("$line to $ipv4Long")

        val correctSetToIpv4Int = if (ipv4Long > Int.MAX_VALUE) {
            // largest possible ipv4 long value is 4294967295 corresponding to 255.255.255.255, which is equal to Int.MAX_VALUE + 1
            // this way we can guarantee that all ipv4 long values can be put in a bitset
            val highIpv4Int = (ipv4Long - Int.MAX_VALUE - 1).toInt()
            if (highIpv4Int == 0) {
                if (highZeroCount == 0) highZeroCount++
                return@forEachLine
            }
            largerValuesBitSet to highIpv4Int
        } else {
            val lowIpv4Int = ipv4Long.toInt()
            if (lowIpv4Int == 0) {
                if (lowZeroCount == 0) lowZeroCount++
                return@forEachLine
            }
            smallerValuesBitSet to lowIpv4Int
        }

        val bitSet = correctSetToIpv4Int.first
        val ipv4Int = correctSetToIpv4Int.second
        if (!bitSet.get(ipv4Int)) {
            bitSet.set(ipv4Int)
        }
    }

    return (smallerValuesBitSet.cardinality() + largerValuesBitSet.cardinality()).toLong() + highZeroCount + lowZeroCount
}

fun String.ipv4ToLong(): Long {
    val octets = split(".")
    var result: Long = 0

    for (i in octets.indices) {
        val octet = octets[i].toLong()
        result = result or (octet shl (8 * (3 - i)))
    }
    return result
}