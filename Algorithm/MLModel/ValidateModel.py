import urllib.request
import json
import os
import ssl
import ImportCsv as csv


def allowSelfSignedHttps(allowed):
    # bypass the server certificate verification on client side
    if allowed and not os.environ.get('PYTHONHTTPSVERIFY', '') and getattr(ssl, '_create_unverified_context', None):
        ssl._create_default_https_context = ssl._create_unverified_context

allowSelfSignedHttps(True) # this line is needed if you use self-signed certificate in your scoring service.

times = csv.LoadCsv(False)
timesValidation = csv.LoadCsv(True)
data = {}
data['data'] = times

body = str.encode(json.dumps(data))

url = 'http://e597454d-6e25-4ecc-b7c9-54387ef56adb.eastus2.azurecontainer.io/score'
api_key = '' # Replace this with the API key for the web service
headers = {'Content-Type':'application/json', 'Authorization':('Bearer '+ api_key)}

req = urllib.request.Request(url, body, headers)

try:
    response = urllib.request.urlopen(req)

    result = response.read()
    responseString = result.decode('UTF-8').replace('\\"','\'').replace('\"', '')
    response = responseString.split('[', )[1].split(']',)[0]

    resultList = response.split(',')
    index = 0
    for i in resultList:
        print("Berechnet: " + i + "Wirklich: " + timesValidation[index]['real'])
        index += 1


except urllib.error.HTTPError as error:
    print("The request failed with status code: " + str(error.code))

    # Print the headers - they include the requert ID and the timestamp, which are useful for debugging the failure
    print(error.info())
    print(json.loads(error.read().decode("utf8", 'ignore')))
