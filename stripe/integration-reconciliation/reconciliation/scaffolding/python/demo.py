#!/usr/bin/env python3

import json
import requests

# See ../1-json-primer.md for an explanation
json_text = '{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}'
data = json.loads(json_text)

demo_letter = data["foo"]["bar"][1]["paint"]
print(demo_letter)  # "green"

data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}

result = json.dumps(data, indent=4)
print(result)

result2 = requests.get("https://httpbin.org/get")
print(result2.text)

result3 = requests.post("https://httpbin.org/post", data={"key": "value"})
print(result3.text)
