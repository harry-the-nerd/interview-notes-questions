#!/usr/bin/env ruby

require 'json'
require 'excon'

# See ../1-json-primer.md for an explanation
json_text = '{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}'
data = JSON.parse(json_text)

demo_letter = data["foo"]["bar"][1]["paint"] # "green"
puts demo_letter

data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}

result = JSON.pretty_generate(data) # or JSON.dump
puts result

resp = Excon.get("https://httpbin.org/get")
puts resp.body

resp2 = Excon.post("https://httpbin.org/post",
                   :body => URI.encode_www_form(:key => 'value'),
                   :headers => { "Content-Type" => "application/x-www-form-urlencoded" })
puts resp2.body
