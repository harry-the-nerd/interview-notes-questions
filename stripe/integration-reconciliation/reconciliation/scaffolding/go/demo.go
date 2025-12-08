// go run demo.go

package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"strings"
)

func main() {
	if err := handleJSON(); err != nil {
		log.Fatal(err)
	}
	if err := handleHTTP(); err != nil {
		log.Fatal(err)
	}
}

// See ../1-json-primer.md for an explanation
func handleJSON() error {
	jsonText := []byte(`
		{
		    "foo": {
		        "bar": [
		            {"paint": "red"},
		            {"paint": "green"},
		            {"paint": "blue"}
		        ]
		    }
		}`)

	// create an instance of our type to contain the unmarshalled JSON data
	var obj JSONObj

	if err := json.Unmarshal(jsonText, &obj); err != nil {
		return err
	}

	// obtain the value of one of the paint colors, at the JSON "path" of: obj["foo"]["bar"][1]["paint"]
	fmt.Printf("paint color: %s\n", obj.Foo.Bar[1].Paint)

	// Add the additional data to the object, per the instructions.
	obj.Quux.Stuff = "nonsense"
	obj.Quux.Nums = []float64{2.718, 3.142}

	// Marshall the whole object back out, formatting the JSON with indents to
	// make it easier to read.
	marshalledBytes, err := json.MarshalIndent(obj, "", "  ")
	if err != nil {
		return err
	}

	fmt.Println(string(marshalledBytes))
	return nil
}

// JSONObj is a type that represents the structure of the example JSON
type JSONObj struct {
	Foo struct {
		Bar []PaintColor `json:"bar"`
	} `json:"foo"`

	Quux struct {
		Stuff string    `json:"stuff"`
		Nums  []float64 `json:"nums"`
	} `json:"quux"`
}

// PaintColor encapsulates a JSON object with the key "paint" and string value
// containing the name of a paint color
// e.g. { "paint": "red" }
type PaintColor struct {
	Paint string `json:"paint"`
}

func handleHTTP() error {
	client := &http.Client{}

	req, err := http.NewRequest(http.MethodGet, "https://httpbin.org/get", nil)
	if err != nil {
		return err
	}
	res, err := client.Do(req)
	if err != nil {
		return err
	}
	resBody, err := ioutil.ReadAll(res.Body);
	if err != nil {
		return err
	}
	fmt.Println("got response: %s", string(resBody));

	data := url.Values{}
	data.Set("key", "value");

	req2, err := http.NewRequest(http.MethodPost, "https://httpbin.org/post", strings.NewReader(data.Encode()))
	req2.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	if err != nil {
		return err
	}
	res2, err := client.Do(req2)
	resBody2, err := ioutil.ReadAll(res2.Body);
	if err != nil {
		return err;
	}
	fmt.Println("got response: %s", string(resBody2));
	return nil
}
