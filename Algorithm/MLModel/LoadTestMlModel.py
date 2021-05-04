import urllib.request
import json
import os
import ssl



def loadData():

    lines = []

    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')
    lines.append('1,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11')
    lines.append('1,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11')
    lines.append('1,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35')
    lines.append('0,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8,2,5,35')
    lines.append('0,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8')
    lines.append('0,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4')
    lines.append('0,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1')
    lines.append('0,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4')
    lines.append('0,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')

    data = []

    for line in lines:
        linedata = line.split(',')

        sample = {}
        sample['sleep'] = (linedata[3])
        sample['sleep1'] = (linedata[6])
        sample['sleep2'] = (linedata[9])
        sample['sleep3'] = (linedata[12])
        sample['sleep4'] = (linedata[15])
        sample['sleep5'] = (linedata[18])
        sample['sleep6'] = (linedata[21])
        sample['sleep7'] = (linedata[24])
        sample['sleep8'] = (linedata[27])
        sample['sleep9'] = (linedata[30])
        sample['brigthness'] = (linedata[1])
        sample['brigthness1'] = (linedata[4])
        sample['brigthness2'] = (linedata[7])
        sample['brigthness3'] = (linedata[10])
        sample['brigthness4'] = (linedata[13])
        sample['brigthness5'] = (linedata[16])
        sample['brigthness6'] = (linedata[19])
        sample['brigthness7'] = (linedata[22])
        sample['brigthness8'] = (linedata[25])
        sample['brigthness9'] = (linedata[28])
        sample['motion'] = (linedata[2])
        sample['motion1'] = (linedata[5])
        sample['motion2'] = (linedata[8])
        sample['motion3'] = (linedata[11])
        sample['motion4'] = (linedata[14])
        sample['motion5'] = (linedata[17])
        sample['motion6'] = (linedata[20])
        sample['motion7'] = (linedata[23])
        sample['motion8'] = (linedata[26])
        sample['motion9'] = (linedata[29])

        data.append(sample)

    return data



def allowSelfSignedHttps(allowed):
    # bypass the server certificate verification on client side
    if allowed and not os.environ.get('PYTHONHTTPSVERIFY', '') and getattr(ssl, '_create_unverified_context', None):
        ssl._create_default_https_context = ssl._create_unverified_context

allowSelfSignedHttps(True) # this line is needed if you use self-signed certificate in your scoring service.



data = {}

data["data"] = []

alldata = loadData()

for samples in alldata:
    calcs = {}
    for entry in samples:
        calcs[entry] = samples[entry]
    
    data["data"].append(calcs)


body = str.encode(json.dumps(data))

url = 'http://1028d77e-7cbf-4b1d-80b6-cffd969c381e.eastus2.azurecontainer.io/score'
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
