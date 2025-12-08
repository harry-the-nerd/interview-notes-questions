#!/usr/bin/env ruby

require 'json'

# See ../1-json-primer.md for an explanation
json_text = '{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}'
data = JSON.parse(json_text)

demo_letter = data["foo"]["bar"][1]["paint"] # "green"
puts demo_letter

data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}

result = JSON.pretty_generate(data) # or JSON.dump
puts result
