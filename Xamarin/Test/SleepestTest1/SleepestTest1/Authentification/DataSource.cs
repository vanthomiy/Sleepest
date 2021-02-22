using System;
using System.Collections.Generic;
using System.Text;

namespace SleepestTest1.Authentification
{
    // Root myDeserializedClass = JsonConvert.DeserializeObject<Root>(myJsonResponse); 
    public class Application
    {
        public string detailsUrl { get; set; }
        public string name { get; set; }
        public string version { get; set; }
    }

    public class Field
    {
        public string name { get; set; }
        public string format { get; set; }
    }

    public class DataType
    {
        public List<Field> field { get; set; }
        public string name { get; set; }
    }

    public class Device
    {
        public string manufacturer { get; set; }
        public string model { get; set; }
        public string type { get; set; }
        public string uid { get; set; }
        public string version { get; set; }
    }

    public class DataSourceRoot
    {
        public string dataStreamName { get; set; }
        public string type { get; set; }
        public Application application { get; set; }
        public DataType dataType { get; set; }
        public Device device { get; set; }
    }
}
