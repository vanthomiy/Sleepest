using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace SleepestTest1.Models
{
    [Serializable]
    public class Value
    {
        public int intVal { get; set; }
        public List<object> mapVal { get; set; }
    }


    [Serializable]
    public class Point
    {
        public string startTimeNanos { get; set; }
        public string endTimeNanos { get; set; }
        public string dataTypeName { get; set; }
        public string originDataSourceId { get; set; }
        public List<Value> value { get; set; }


    }

    [Serializable]
    public class Dataset
    {
        public string dataSourceId { get; set; }
        public List<Point> point { get; set; }

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

     
    }
}
