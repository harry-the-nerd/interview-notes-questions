package com.example.interview

import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import com.jsoniter.spi.Config
import com.jsoniter.any.Any as AnyJson

private fun handleJson() {
    val jsonText: String = "{\"foo\": {\"bar\": [{\"paint\": \"red\"}, {\"paint\": \"green\"}, {\"paint\": \"blue\"}]}}"
    val data: AnyJson = JsonIterator.deserialize(jsonText)

    // Use toString(), toDouble(), toInt(), etc with types
    val demoLetter: String = data.toString("foo", "bar", 1, "paint")
    println("toString: $demoLetter") // "green"

    // Or use get() and Any
    val demoLetter2: AnyJson = data["foo", "bar", 1, "paint"]
    println("Any: $demoLetter2") // "green"

    val quux: Map<String, Any> = mapOf(
        "stuff" to "nonsense",
        "nums" to listOf(2.718, 3.142),
    )

    // data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}
    data["foo"].asMap()["quux"] = AnyJson.wrap(quux)

    // Use Config to get pretty-printed indentation
    val cfg: Config = Config.Builder().indentionStep(2).build()

    // serialize object to string as json
    val result: String = JsonStream.serialize(cfg, data)
    println(result)
}

fun main(args: Array<String>) {
    handleJson()
}