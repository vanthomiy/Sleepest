using System;
using System.Collections.Generic;
using System.Text;

namespace SleepestTest1.FitnessData.Builder
{
    public class RequestBuilder
    {

        static Dictionary<RequestType, Uri> RequestUri = new Dictionary<RequestType, Uri>()
        {
            {RequestType.AllDataSources, new Uri("https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate") }
        };

        static Dictionary<RequestType, string> RequestBody = new Dictionary<RequestType, string>()
        {
            {RequestType.AllDataSources, "{\"aggregateBy\":[{\"dataSourceId\":\"derived:com.google.step_count.delta:com.google.android.gms:estimated_steps\"}],\"bucketByTime\":{\"durationMillis\":10000000},\"startTimeMillis\":1614497276586,\"endTimeMillis\":1614507054818}" }
        };


        //public static Models.Request CreateRequest(RequestType reqType, )
        //{

        //}



    }


    public enum RequestType
    {
        AllDataSources,

    }
}
