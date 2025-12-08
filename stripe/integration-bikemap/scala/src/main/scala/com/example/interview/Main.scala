package com.example.interview

object Main extends App {

  // See 1-json-primer.md for an explanation
  val json_text = """{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}"""
  val data = ujson.read(json_text)

  // Use .str, .num (for double), .num.toInt (for int), .obj, .arr with types.
  val demoLetter: String = data("foo")("bar")(1)("paint").str
  println(s"str: ${demoLetter}") // "green"

  // data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}
  val quux = ujson.Obj(
    "stuff" -> "nonsense",
    "num" -> ujson.Arr(2.718, 3.142)
  )
  data("foo")("quux") = quux

  // serialize object to string as json
  val result: String = ujson.write(data, indent=2)
  println(result)
}
