# Kotlin

* We recommend that you use the [jsoniter] library
  to manipulate JSON as unstructured objects.
  However, you may use any JSON library you like.
* See [App.kt] for an implementation of the JSON handling
  described in the [JSON Primer].
* We recommend [OkHttp] for HTTP.

See also [Fun with Any](https://www.sitepoint.com/php-style-json-parsing-in-java-with-jsoniter/#funwithany)
for more on jsoniter.

## Running
To compile and run this code with [Gradle]:

```text
./gradlew :app:run
```

## Using IntelliJ IDEA

To open the project in IntelliJ IDEA:

- Ensure the bundled "Kotlin" & "Gradle" plugins are enabled.
- Select `File > New > Project from Existing Sources...` from the menu.
- Navigate in the file selector to this `kotlin` subdirectory.
- Ensure "Import project from external model" and "Gradle" are selected, click Finish.

A new window should then appear with the project.

[jsoniter]: https://jsoniter.com/
[App.kt]: ./app/src/main/kotlin/com/stripe/interview/App.kt
[JSON Primer]: ../1-json-primer.md
[OkHttp]: https://square.github.io/okhttp/
[Gradle]: https://gradle.org/
[Fun with Any]: https://www.sitepoint.com/php-style-json-parsing-in-java-with-jsoniter/#funwithany
