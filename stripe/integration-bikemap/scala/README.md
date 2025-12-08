# Scala

* We recommend that you use the [uPickle] library
  to manipulate JSON as unstructured objects.
  However, you may use any JSON library you like.
* See [Main.scala] for an implementation of the JSON handling
  described in the [JSON Primer].
* We recommend [OkHttp] for HTTP.

See also [How to work with JSON in Scala] for more on uPickle.

## Running
To compile and run this code with [SBT]:

```text
sbt run
```

To run the tests:
```text
sbt test
```

# Useful References

 * [Getting Started with sbt](http://www.scala-sbt.org/release/tutorial/)
 * [Installing sbt on Windows](http://www.scala-sbt.org/0.13/tutorial/Installing-sbt-on-Windows.html)
 * IDE Specific Instructions:
   * [Intellij IDEA](https://www.jetbrains.com/idea/help/getting-started-with-sbt.html)
   * [Eclipse / Scala IDE](http://scala-ide.org/docs/user/gettingstarted.html#Import_an_SBT_project)


[uPickle]: https://github.com/com-lihaoyi/upickle
[Main.scala]: ./src/main/scala/com/stripe/interview/Main.scala
[JSON Primer]: ../1-json-primer.md
[OkHttp]: https://square.github.io/okhttp/
[SBT]: https://www.scala-sbt.org/
[How to work with JSON in Scala]: https://www.lihaoyi.com/post/HowtoworkwithJSONinScala.html
