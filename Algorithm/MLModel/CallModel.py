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
            'Datum': "example_value",
            'Uhrzeit': "example_value",
            'Schlaf': "100",
            'Licht': "0",
            'Bewegung': "0",
            'Schlafphase Kalkuliert': "0",
        },
        {
            'Datum': "example_value",
            'Uhrzeit': "example_value",
            'Schlaf': "100",
            'Licht': "0",
            'Bewegung': "0",
            'Schlafphase Kalkuliert': "0",
        },
        {
            'Datum': "example_value",
            'Uhrzeit': "example_value",
            'Schlaf': "0",
            'Licht': "0",
            'Bewegung': "0",
            'Schlafphase Kalkuliert': "0",
        }
    ],
}

body = str.encode(json.dumps(data))

url = 'http://daa659e6-226e-482b-a208-ad84a52ac13b.eastus2.azurecontainer.io/score'
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
