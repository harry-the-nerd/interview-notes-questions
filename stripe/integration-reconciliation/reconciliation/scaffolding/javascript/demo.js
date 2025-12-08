#!/usr/bin/env node

import axios from "axios";
import qs from "qs";

// See ../1-json-primer.md for an explanation
var json_text = '{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}';
var data = JSON.parse(json_text);

var demo_letter = data["foo"]["bar"][1]["paint"];  // "green"
var demo_letter2 = data.foo.bar[1].paint;  // JS dotted object syntax
console.log(demo_letter);

data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}

var result = JSON.stringify(data, null, 4);
console.log(result);

const resp = await axios.get("https://httpbin.org/get");
console.log(resp.data);

const formData = new FormData();
formData.append("key", "value");

const resp2 = await axios.post("https://httpbin.org/post", data=qs.stringify({"key": "value"}));
console.log(resp2.data);
