﻿using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.OS;
using SleepestTest1.Authentification;
using System;

namespace SleepestTest1.Droid
{
	[Activity(Label = "CustomUrlSchemeInterceptorActivity", NoHistory = true, LaunchMode = LaunchMode.SingleTop)]
	[IntentFilter(
   new[] { Intent.ActionView },
   Categories = new[] { Intent.CategoryDefault, Intent.CategoryBrowsable },
   DataSchemes = new[] { "com.googleusercontent.apps.951458878515-gu0je92adp23lb3lofuro7kb36skdrkj" },
   DataPath = "/oauth2redirect")]
	public class CustomUrlSchemeInterceptorActivity : Activity
	{
		protected override void OnCreate(Bundle savedInstanceState)
		{
			base.OnCreate(savedInstanceState);

			// Convert Android.Net.Url to Uri
			var uri = new Uri(Intent.Data.ToString());

			// Load redirectUrl page
			AuthenticationState.Authenticator.OnPageLoading(uri);

			this.Finish();

			return;
		}
	}
}