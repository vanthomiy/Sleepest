using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace SleepestTest1.Models
{
    [Serializable]
    public class Dataset
    {
        public string dataSourceId { get; set; }
        public List<object> point { get; set; }
    }

    [Serializable]
    public class Bucket
    {
        public string startTimeMillis { get; set; }
        public string endTimeMillis { get; set; }
        public List<Dataset> dataset { get; set; }
    }

    [Serializable]
    public class Session
    {
        public List<Bucket> bucket { get; set; }

        public static string convertJson(string response)
        {
            //string json = @"{'bucket': [{'startTimeMillis': '1575591360000','endTimeMillis': '1575609060000','dataset': [{'dataSourceId': 'derived:com.google.sleep.segment:com.google.android.gms:merged','point': []}]}]}";
            Session session = JsonConvert.DeserializeObject<Session>(response);
            string puf = "";
            return puf;
        }
    }
}
