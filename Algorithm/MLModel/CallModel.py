import urllib.request
import json
import os
import ssl

def allowSelfSignedHttps(allowed):
    # bypass the server certificate verification on client side
    if allowed and not os.environ.get('PYTHONHTTPSVERIFY', '') and getattr(ssl, '_create_unverified_context', None):
        ssl._create_default_https_context = ssl._create_unverified_context

allowSelfSignedHttps(True) # this line is needed if you use self-signed certificate in your scoring service.

f = open('Fabi1615561080.json', encoding='utf-8-sig')
text = f.read()

dataJson = json.loads(text)


f.close()


data = {
    "data":
    [
        {
            'time': "2000-01-01 00:00:00,000000",
            ' light': "example_value",
            ' motion': "example_value",
            ' sleep': "example_value",
            ' real': "example_value",
            ' user': "example_value",
        },
    ],
}
data['data'] = dataJson

body = str.encode(json.dumps(data))

url = 'http://ceb553de-6a0b-4465-bf36-6475ff6bcb83.eastus2.azurecontainer.io/score'
api_key = '' # Replace this with the API key for the web service
headers = {'Content-Type':'application/json', 'Authorization':('Bearer '+ api_key)}

req = urllib.request.Request(url, body, headers)

try:
    response = urllib.request.urlopen(req)

    result = response.read()
    print(result)
except urllib.error.HTTPError as error:
    print("The request failed with status code: " + str(error.code))

    # Print the headers - they include the requert ID and the timestamp, which are useful for debugging the failure
    print(error.info())
    print(json.loads(error.read().decode("utf8", 'ignore')))
