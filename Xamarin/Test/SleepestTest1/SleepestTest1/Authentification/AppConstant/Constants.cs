using System;
using System.Collections.Generic;
using System.Text;

namespace SleepestTest1.AppConstant
{
	public class Constants
	{
		public static string AppName = "OAuthNativeFlow";
		public static string GoogleData = "GOOGLE_DATA";

		// OAuth
		// For Google login, configure at https://console.developers.google.com/
		public static string iOSClientId = "844060696235-gtoiepn6u6trvaoh5s6uo1a1a3hrcrnq.apps.googleusercontent.com";
		public static string AndroidClientId = "951458878515-gu0je92adp23lb3lofuro7kb36skdrkj.apps.googleusercontent.com";

		// These values do not need changing
		public static string Scope = "https://www.googleapis.com/auth/fitness.sleep.read https://www.googleapis.com/auth/fitness.sleep.write " +
			"https://www.googleapis.com/auth/fitness.activity.read https://www.googleapis.com/auth/fitness.activity.read " +
			"https://www.googleapis.com/auth/fitness.heart_rate.read ";
		public static string AuthorizeUrl = "https://accounts.google.com/o/oauth2/auth";
		public static string AccessTokenUrl = "https://www.googleapis.com/oauth2/v4/token";
		public static string UserInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		public static string SleepDataUrl = "https://www.googleapis.com/fitness/v1/users/me/dataSources";

		// Set these to reversed iOS/Android client ids, with :/oauth2redirect appended
		public static string iOSRedirectUrl = "com.googleusercontent.apps.951458878515-gu0je92adp23lb3lofuro7kb36skdrkj:/oauth2redirect";
		public static string AndroidRedirectUrl = "com.googleusercontent.apps.951458878515-gu0je92adp23lb3lofuro7kb36skdrkj:/oauth2redirect";
	}
}
