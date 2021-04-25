import urllib.request
import json
import os
import ssl

def allowSelfSignedHttps(allowed):
    # bypass the server certificate verification on client side
    if allowed and not os.environ.get('PYTHONHTTPSVERIFY', '') and getattr(ssl, '_create_unverified_context', None):
        ssl._create_default_https_context = ssl._create_unverified_context

allowSelfSignedHttps(True) # this line is needed if you use self-signed certificate in your scoring service.

data = {
    "data":
    [
        {
            'time': "2000-01-01 00:00:00,000000",
            'light': "0",
            'motion': "0",
            'sleep': "0",
            'light1': "0",
            'motion1': "0",
            'sleep1': "0",
            'light2': "0",
            'motion2': "0",
            'sleep2': "0",
            'light3': "0",
            'motion3': "0",
            'sleep3': "0",
            'light4': "0",
            'motion4': "0",
            'sleep4': "0",
            'light5': "0",
            'motion5': "0",
            'sleep5': "0",
            'light6': "0",
            'motion6': "0",
            'sleep6': "0",
            'light7': "0",
            'motion7': "0",
            'sleep7': "0",
            'light8': "0",
            'motion8': "0",
            'sleep8': "0",
            'light9': "0",
            'motion9': "0",
            'sleep9': "0",
        },
    ],
}

body = str.encode(json.dumps(data))

url = 'http://b8d7fa49-7a6c-4bf0-ae14-d1ff37c48061.eastus2.azurecontainer.io/score'
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
