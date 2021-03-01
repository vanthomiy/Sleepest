using Newtonsoft.Json;
using SleepestTest1.FitnessData.Models;
using System;
using System.Collections.Generic;
using System.Text;

namespace SleepestTest1.FitnessData.Builder
{
    public class FitRequestBuilder<T>
    {
        public static Dictionary<DataSourceType, string> RequestDataType = new Dictionary<DataSourceType, string>()
        {
            {DataSourceType.StepCountEstaminated, "derived:com.google.step_count.delta:com.google.android.gms:estimated_steps" },
            {DataSourceType.Weight, "derived:com.google.weight:com.google.android.gms:merge_weight" },
            {DataSourceType.StepCountMerge, "derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas" }, // value that is displayed in fit
            {DataSourceType.SleepSegments, "derived:com.google.sleep.segment:com.google.android.gms:merged" }, // sleep data from the clock
            {DataSourceType.Height, "derived:com.google.height:com.google.android.gms:merge_height" },
            {DataSourceType.HeartRaterest, "derived:com.google.heart_rate.bpm:com.google.android.gms:resting_heart_rate<-merge_heart_rate_bpm" }, // Heart rate 6 values
            {DataSourceType.HeartRate, "derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm" }, // Viel mehr values > 100
            {DataSourceType.HeartMinutes, "derived:com.google.heart_minutes:com.google.android.gms:merge_heart_minutes" }, // keine daten
            {DataSourceType.HeartMinutesBpm, "derived:com.google.heart_minutes:com.google.android.gms:from_heart_rate<-merge_heart_rate_bpm" }, // keine daten
            {DataSourceType.HeartMinutesSegments, "derived:com.google.heart_minutes:com.google.android.gms:from_activity<-merge_activity_segments" },// keine daten
            {DataSourceType.Calories, "derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended" }, // calorien über die zeit
            {DataSourceType.ActivitySegments, "derived:com.google.activity.segment:com.google.android.gms:merge_activity_segments" }, // schlaf segmente
            {DataSourceType.ActivityMinutes, "derived:com.google.active_minutes:com.google.android.gms:merge_active_minutes" }, // active minutes ??
            {DataSourceType.ActiveSegments, "derived:com.google.active_minutes:com.google.android.gms:from_activity<-merge_activity_segments" } // nothing
        };

        static Dictionary<RequestType, Uri> RequestUri = new Dictionary<RequestType, Uri>()
        {
            {RequestType.AllDataSources, new Uri("https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate") }
        };

        public static FitRequest CreateRequest(RequestType reqType, T obj)
        {
            return new FitRequest() { uri = RequestUri[reqType], requestBody = JsonConvert.SerializeObject(obj) };
        }

    }


    public enum RequestType
    {
        AllDataSources,

    }

    public enum DataSourceType
    {
        StepCountEstaminated,
        Weight,
        StepCountMerge,
        SleepSegments,
        Height,
        HeartRaterest,
        HeartRate,
        HeartMinutes,
        HeartMinutesBpm,
        HeartMinutesSegments,
        Calories,
        ActivitySegments,
        ActivityMinutes,
        ActiveSegments
    }
    
}
