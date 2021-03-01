using System;
using System.Collections.Generic;
using System.Text;

namespace SleepestTest1.FitnessData.Models
{
   
    [Serializable]
    public class DataSourcesRequest
    {
        [Serializable]
        public class AggregateBy
        {
            public string dataSourceId { get; set; }
        }

        public List<AggregateBy> aggregateBy { get; set; }
        public long startTimeMillis { get; set; }
        public long endTimeMillis { get; set; }




        public DataSourcesRequest(List<AggregateBy> aggregateBy, long startTimeMillis, long endTimeMillis)
        {
            this.aggregateBy = aggregateBy;
            this.startTimeMillis = startTimeMillis;
            this.endTimeMillis = endTimeMillis;
        }

        public DataSourcesRequest(string aggregateByOne, DateTimeOffset since)
        {
            aggregateBy = new List<AggregateBy>();
            aggregateBy.Add(new AggregateBy() { dataSourceId = aggregateByOne });

            startTimeMillis = since.ToUnixTimeMilliseconds();
            endTimeMillis = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
    }

    [Serializable]
    public class DataSources
    {
        [Serializable]
        public class Field
        {
            public string name { get; set; }
            public string format { get; set; }
        }

        [Serializable]
        public class DataType
        {
            public List<Field> field { get; set; }
            public string name { get; set; }
        }

        [Serializable]
        public class Application
        {
            public string packageName { get; set; }
        }

        [Serializable]
        public class Device
        {
            public string model { get; set; }
            public string version { get; set; }
            public string type { get; set; }
            public string uid { get; set; }
            public string manufacturer { get; set; }
        }

        [Serializable]
        public class DataSource
        {
            public List<object> dataQualityStandard { get; set; }
            public DataType dataType { get; set; }
            public string dataStreamName { get; set; }
            public Application application { get; set; }
            public string dataStreamId { get; set; }
            public string type { get; set; }
            public Device device { get; set; }
        }

        public List<DataSource> dataSource { get; set; }
    }
}
