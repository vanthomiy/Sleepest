using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Xamarin.Forms;

namespace TestApp1.Droid.Models
{
    class AndroidAlarmHandler
    {

        public void SendNotification(string message, string title)
        {
            //DateTime time = DateTime.Now.AddMinutes(1);

            //Intent alarmIntent = new Intent(Forms.Context, typeof(testReceiver));
            //alarmIntent.PutExtra("message", message);
            //alarmIntent.PutExtra("title", title);

            //PendingIntent pendingIntent = PendingIntent.GetBroadcast(Forms.Context, 0, alarmIntent, PendingIntentFlags.UpdateCurrent);
            //AlarmManager alarmManager = (AlarmManager)Forms.Context.GetSystemService(Context.AlarmService);

            //DateTime date = DateTime.Now.AddSeconds(30);
            //TimeSpan span = (date - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));


            //long timer = (long)span.TotalMilliseconds;
            ////long timer2 = SystemClock.ElapsedRealtime() + 30 * 1000;

            ////alarmManager.Set(AlarmType.ElapsedRealtime, timer2, pendingIntent);
            //alarmManager.Set(AlarmType.RtcWakeup, DateTime.Now.Millisecond + 30000, pendingIntent);
        }

    }    
}