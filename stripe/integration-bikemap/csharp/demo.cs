using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Demo
{
    class Program
    {
        static void Main(string[] args)
        {
            handle_json();
        }

        // See ../1-json-primer.md for an explanation
        static void handle_json()
        {
            string json_text = "{\"foo\": {\"bar\": [{\"paint\": \"red\"}, {\"paint\": \"green\"}, {\"paint\": \"blue\"}]}}";
            dynamic data = JsonConvert.DeserializeObject(json_text);

            string demo_letter = data.foo.bar[1].paint;  // "green"
            Console.WriteLine(demo_letter);

            // data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}
            data.foo.quux = JObject.FromObject(
                new {
                    stuff = "nonsense",
                    nums = new[] {2.718, 3.142}
                }
            );

            string result = JsonConvert.SerializeObject(data, Formatting.Indented);
            Console.WriteLine(result);
        }
    }
}
