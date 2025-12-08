# Rust

* Use [serde_json] to manipulate untyped JSON *or* typed JSON structs.
* See [src/main.rs](./src/main.rs) for an implementation of the JSON handling
  described in the [JSON Primer].
* We recommend [reqwest] or [reqwest-blocking] for HTTP.

To run this code:

```bash
cargo run
```

[serde_json]: https://docs.rs/serde_json/latest/serde_json/
[JSON Primer]: ../1-json-primer.md
[reqwest]: https://docs.rs/reqwest/latest/reqwest/
[reqwest-blocking]: https://docs.rs/reqwest/latest/reqwest/blocking/index.html
