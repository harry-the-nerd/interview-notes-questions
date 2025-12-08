# JSON Primer

[JSON] (JavaScript Object Notation)
is a *text-based* data interchange format.

[JSON]: https://www.json.org/

Here, we'll show you how to parse a JSON *string*
into an object in a variety of programming languages.

In some languages (like Python, Ruby, and JavaScript), this is more easily
accomplished using *unstructured objects* (aka *dynamic objects*), while in
others (like Go), the approach typically involves defining some types to parse
the JSON data into.

We suggest using whichever approach is more idiomatic for the language being
used.

Given this JSON *text*:

```json
{
    "foo": {
        "bar": [
            {"paint": "red"},
            {"paint": "green"},
            {"paint": "blue"}
        ]
    }
}
```

which can also be written on one line as:

```json
{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}
```

We want to parse this string into an object called `data`,
then **extract** a field several levels down:

```js
data["foo"]["bar"][1]["paint"]
```

namely the string value `"green"`.

We'll also **construct** a new compound object
and **add** it as a subobject of `data`:

```js
data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}
```

which updates the structure to:

```json
{
    "foo": {
        "bar": [
            {"paint": "red"},
            {"paint": "green"},
            {"paint": "blue"}
        ],
        "quux": {
            "stuff": "nonsense",
            "nums": [
                2.718,
                3.142
            ]
        }
    }
}
```

Note: implementations may sort the fields
(`bar`/`quux` and `stuff`/`nums`) differently
when serializing objects to JSON.

## Sample Code for JSON Object Manipulation

You can find working *sample code* for JSON handling
of the above example
in subdirectories of this repo.

* [C++](./cpp/README.md)
* [C#](./csharp/README.md)
* [Go](./go/README.md)
* [Java](./java/README.md)
* [JavaScript](./javascript/README.md)
* [Kotlin](./kotlin/README.md)
* [Python](./python/README.md)
* [Ruby](./ruby/README.md)
* [Rust](./rust/README.md)

## Other Languages

* Clojure: Use [data.json]
* Common Lisp: Use [cl-json]
* PHP: use [php-json]

[data.json]: http://clojure.github.io/data.json/
[cl-json]: https://common-lisp.net/project/cl-json/cl-json.html
[php-json]: https://www.php.net/manual/en/book.json.php
