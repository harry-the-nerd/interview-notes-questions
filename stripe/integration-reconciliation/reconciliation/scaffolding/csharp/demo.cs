using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Demo
{
    class Program
    {
        static HttpClient client = new HttpClient();

        static void Main(string[] args)
        {
            handle_json();
            handle_http().GetAwaiter().GetResult();
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

        static async Task handle_http()
        {
          HttpResponseMessage resp = await client.GetAsync("https://httpbin.org/get");
          Console.WriteLine(await resp.Content.ReadAsStringAsync());

          var dict = new Dictionary<string, string>();
          dict.Add("key", "value");

          HttpResponseMessage resp2 = await client.PostAsync("https://httpbin.org/post", new FormUrlEncodedContent(dict));
          Console.WriteLine(await resp2.Content.ReadAsStringAsync());

        }
    }
}
