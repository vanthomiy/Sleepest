using System;
using System.Collections.Generic;
using System.Text;

namespace SleepestTest1.AppConstant
{
	public class Constants
	{
		public static string AppName = "OAuthNativeFlow";
		public static string GoogleAccount = "GOOGLE_ACCOUNT"; // Key to store/retrive the account in SecurityStorage
		public static string GoogleTokenExpires = "GOOGLE_TOKEN_EXPIRES"; // Key to store/retrive the expires time in SecurityStorage

		// OAuth
		public static string AndroidClientId = "951458878515-gu0je92adp23lb3lofuro7kb36skdrkj.apps.googleusercontent.com";

		// These values do not need changing
		public static string Scope = 
			"https://www.googleapis.com/auth/fitness.sleep.read https://www.googleapis.com/auth/fitness.sleep.write " +
			"https://www.googleapis.com/auth/fitness.activity.read https://www.googleapis.com/auth/fitness.activity.read " +
			"https://www.googleapis.com/auth/fitness.heart_rate.read ";
		public static string AuthorizeUrl = "https://accounts.google.com/o/oauth2/auth";
		public static string AccessTokenUrl = "https://www.googleapis.com/oauth2/v4/token";
		public static string UserInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		public static string SleepDataUrl = "https://www.googleapis.com/fitness/v1/users/me/dataSources";
		public static string RefreshUrl = "https://www.googleapis.com/oauth2/v4/token";
		
		public static string AndroidRedirectUrl = "com.googleusercontent.apps.951458878515-gu0je92adp23lb3lofuro7kb36skdrkj:/oauth2redirect";
	}
}
