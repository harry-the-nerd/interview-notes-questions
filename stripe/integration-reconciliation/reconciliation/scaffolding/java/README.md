# Java

* We recommend that you use the [jsoniter] library
  to manipulate JSON as unstructured objects.
  However, you may use any JSON library you like.
* See [App.java] for an implementation of the JSON handling
  described in the [JSON Primer].
* We recommend [OkHttp] for HTTP.

See also [Fun with Any](https://www.sitepoint.com/php-style-json-parsing-in-java-with-jsoniter/#funwithany)
for more on jsoniter.

## Running
To compile and run this code with [Maven]:

```text
mvn install
mvn compile
mvn exec:java
```

## Using Eclipse

To open the project in Eclipse:

- Navigate to: `File > Import > Maven > Existing Maven Project`
- For "Root Directory" select the `bikemap/java` folder
- Make sure the project is selected and click "Finish"

After a few moments, the project will appear in the navigator under "demo".

## Using IntelliJ IDEA

To open the project in IntelliJ IDEA:

- Ensure the bundled "Maven Integration" plugin is enabled.
- Select `File > New > Project from Existing Sources...` from the menu.
- Navigate in the file selector to this `java` subdirectory.
- Ensure "Import project from external model" and "Maven" are selected, click Next.
- You can accept the defaults on the import project dialog. Click Next.
- Leave the profile unselected. Click Next.
- The `com.example.interview:demo:1.0-SNAPSHOT` project should be selected. If not, select it. Click Next.
- Set Project SDK to JDK 1.8. Click Next.
- Pick a name for the project or just accept the default. Click Finish.

A new window should then appear with the project.

[jsoniter]: https://jsoniter.com/
[App.java]: ./src/main/java/com/stripe/interview/App.java
[JSON Primer]: ../1-json-primer.md
[OkHttp]: https://square.github.io/okhttp/
[Maven]: https://maven.apache.org/
[Fun with Any]: https://www.sitepoint.com/php-style-json-parsing-in-java-with-jsoniter/#funwithany
