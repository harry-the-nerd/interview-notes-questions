#include <iostream>

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

    // explicit serialization to string
    std::string s = j.dump();
    std::cout << ".dump() " << s << std::endl;

    // access specific value using JSON Pointer spec (https://tools.ietf.org/html/rfc6901)
    auto demo_letter = j["/foo/bar/1/paint"_json_pointer];
    std::cout << ".get() " << demo_letter.get<std::string>() << std::endl;

    // create a new json object and add it to the prev one
    json j2;
    // add a string
    j2["stuff"] = "nonsense";
    // add an array that is stored as std::vector
    j2["nums"] = {2.718, 3.142};
    // add j2 to j
    j["foo"]["quux"] = j2;

    // pretty print by passing an int for indentation to dump
    std::cout << j.dump(4);
}

int main(int argc, char *argv[]) {
    std::cout << "Hello World!" << std::endl;
    handle_json();
    return 0;
}
