import json
import time
import requests

class ClearingFileGenerator:
  def __init__(self):
    self.ARN = ["0"] * 22
    self.idx = 0

  def get_sum(self, data):
    sum = 0
    for d in data:
      sum += d["amt"]
    return sum

  def ARN_generator(self):
    if self.idx < 10:
      self.ARN[-1] = str(self.idx)
    self.idx += 1
    return "".join(self.ARN)

  def amount_generator(self, amount):
    result = amount
    length = len(result)
    while length < 10:
      result = "0" + result
      length += 1
    return result

  def get_clearing_file(self, file_path):
    clearing_file = ""
    with open(file_path, "r") as f:
      data = json.load(f)
      time_secs = str(int(time.time() * 1000))
      timestamp = "000000"+time_secs

      for d in data:
        ARN = self.ARN_generator()
        amount = self.amount_generator(str(d["amt"]))
        currency = str(d["currency"])
        clearing_file += f"{ARN},{timestamp},{amount},{currency} \n"
    return clearing_file

file_path = "../../part1/payment_intent_fixture_01.json"

generator = ClearingFileGenerator()
clearing_file = generator.get_clearing_file(file_path)

base_url = "https://reconciliation-uw5gplbdim.stripesandbox.com/"
API_GET_KEY = "create_api_key"

response = requests.post(base_url + API_GET_KEY)
API_KEY = response.text

API_SUBMIT_FILE = "/submit_clearing_file"

response = requests.post(base_url + API_SUBMIT_FILE,
                         headers={
                           'Authorization': f"Bearer {API_KEY}",
                           'Content-Type': 'application/x-www-form-urlencoded'},
                         data= {"clearing_file_contents": clearing_file}
                         )
print(response.status_code)
