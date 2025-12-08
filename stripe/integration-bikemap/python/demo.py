#!/usr/bin/env python3

import json

# See ../1-json-primer.md for an explanation
json_text = '{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}'
data = json.loads(json_text)

demo_letter = data["foo"]["bar"][1]["paint"]
print(demo_letter)  # "green"

data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}

result = json.dumps(data, indent=4)
print(result)
