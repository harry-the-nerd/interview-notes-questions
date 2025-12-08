#include <iostream>
#include <fstream>

#include "cpr/cpr.h"
#include "json/json.hpp"

void handle_json() {
    using nlohmann::json;
    // raw, multiline string literal
    std::string jsonString = R"(
        {
            "foo": {
                "bar": [
                    {"paint": "red"},
                    {"paint": "green"},
                    {"paint": "blue"}
                ]
            }
        }
    )";
    // create from string
    json j = json::parse(jsonString);

    std::ifstream ifs("demo.json");
    json j2 = json::parse(ifs);

    // explicit serialization to string
    std::string s = j.dump();
    std::cout << ".dump() " << s << std::endl;

    // access specific value using JSON Pointer spec (https://tools.ietf.org/html/rfc6901)
    auto demo_letter = j["/foo/bar/1/paint"_json_pointer];
    std::cout << ".get() " << demo_letter.get<std::string>() << std::endl;

    // create a new json object and add it to the prev one
    json j3;
    // add a string
    j3["stuff"] = "nonsense";
    // add an array that is stored as std::vector
    j3["nums"] = {2.718, 3.142};
    // add j3 to j
    j["foo"]["quux"] = j3;

    // pretty print by passing an int for indentation to dump
    std::cout << j.dump(4) << std::endl;
}

void handle_http() {
  cpr::Response r = cpr::Get(cpr::Url{"https://www.httpbin.org/get"});
  std::cout << "text " << r.text << std::endl;

  cpr::Response r2 = cpr::Post(cpr::Url{"http://www.httpbin.org/post"},
      cpr::Payload{{"key", "value"}});
  std::cout << "text " << r2.text << std::endl;
}

int main(int argc, char *argv[]) {
    std::cout << "Hello World!" << std::endl;
    handle_json();
    handle_http();
    return 0;
}
