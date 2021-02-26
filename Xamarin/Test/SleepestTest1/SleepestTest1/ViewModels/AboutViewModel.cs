using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification;
using SleepestTest1.Authentification.Service;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Windows.Input;
using Xamarin.Auth;
using Xamarin.Essentials;
using Xamarin.Forms;

namespace SleepestTest1.ViewModels
{
    public class AboutViewModel : BaseViewModel
    {
        #region Xaml Bindings
        private string authState = "Not Authentificated";

		public string AuthState
		{
            get { return authState; }
            set { 
				authState = value;
				OnPropertyChanged();
			}
		}

        private string requestUrl = AppConstant.Constants.SleepDataUrl;

		public string RequestUrl
		{
            get { return requestUrl; }
            set { requestUrl = value;
				OnPropertyChanged();
			}
		}

		private string textResponse;

		public string TextResponse
		{
			get { return textResponse; }
			set { textResponse = value;
				OnPropertyChanged();
			}
		}

		private bool authSuccess = false;

        public bool AuthSuccess
		{
            get { return authSuccess; }
            set { authSuccess = value;
				OnPropertyChanged();
			}
		}

		private bool canRequest = false;

		public bool CanRequest
		{
			get { return canRequest; }
			set { canRequest = value;
				OnPropertyChanged();
			}
		}

        #endregion

        public AboutViewModel()
        {
            Title = "About";
            LoginCommand = new Command(StartAuth);
			GetCommand = new Command(GetRequest);
			PostCommand = new Command(PostRequest);
		}

		public ICommand LoginCommand { get; }
        public ICommand GetCommand { get; }
        public ICommand PostCommand { get; }

        [Obsolete]
        public void StartAuth()
        {
			AuthState = "Wait for response";
			string clientId = null;
			string redirectUri = null;

			switch (Xamarin.Forms.Device.RuntimePlatform)
			{
				case Xamarin.Forms.Device.iOS:
					clientId = AppConstant.Constants.iOSClientId;
					redirectUri = AppConstant.Constants.iOSRedirectUrl;
					break;

				case Xamarin.Forms.Device.Android:
					clientId = AppConstant.Constants.AndroidClientId;
					redirectUri = AppConstant.Constants.AndroidRedirectUrl;
					break;
			}

			//account = store.FindAccountsForService(AppConstant.Constants.AppName).FirstOrDefault();

			/// todo check if accesstoken is available, then dont use authentificator...
			/// Check with valid time when we have to refresh the token again! and store in account
			/// Expires in müsste eig nicht abgefragt werden denke ich
			/// Nur allgemein mal schauen was passiert wenn der token expired und man einen request sendet
			//if (account.Properties.ContainsKey("access_token") && account.Properties["access_token"] != "")
			//{
			//	// check when its expires
			//	if (account.Properties.ContainsKey("expires_in") && account.Properties["expires_in"] != "" )
			//	{
			//		int expiresIn = 0;
			//		Int32.TryParse(account.Properties["expires_in"], out expiresIn);

   //                 if (expiresIn > 100)
   //                 {
			//			AuthSuccess = CanRequest = true;
			//			return;
			//		}
			//	}	
			//}

			var authenticator = new OAuth2Authenticator(
				clientId,
				null,
				AppConstant.Constants.Scope,
				new Uri(AppConstant.Constants.AuthorizeUrl),
				new Uri(redirectUri),
				new Uri(AppConstant.Constants.AccessTokenUrl),
				null,
				true);

			authenticator.Completed += OnAuthCompleted;
			authenticator.Error += OnAuthError;
			authenticator.IsLoadableRedirectUri = true;
			AuthenticationState.Authenticator = authenticator;

			var presenter = new Xamarin.Auth.Presenters.OAuthLoginPresenter();
			presenter.Login(authenticator);
		}

        public async void GetRequest()
        {
			CanRequest = false;
            // If the user is authenticated, request their basic user data from Google
            var request = new OAuth2Request("GET", new Uri(RequestUrl), null, account);
            var response = await request.GetResponseAsync();

            if (response != null)
            {
                // Deserialize the data and store it in the account store
                // The users email address will be used to identify data in SimpleDB
                string userJson = await response.GetResponseTextAsync();
                TextResponse = userJson;
                //dataSource = JsonConvert.DeserializeObject<DataSourceRoot>(userJson);
            }


            // var response = await ProviderService.GetGoogleAsync("https://www.googleapis.com/fitness/v1/users/me/dataSources");


            //if (response == null)
            //{
            //    return;
            //}

            CanRequest = true;

		}

		public async void PostRequest()
		{
			CanRequest = false;

			Dictionary<string, string> content = new Dictionary<string, string>();


			// If the user is authenticated, request their basic user data from Google
			var request = new OAuth2Request("POST", new Uri(RequestUrl), null, account);
			var response = await request.GetResponseAsync();

			if (response != null)
			{
				// Deserialize the data and store it in the account store
				// The users email address will be used to identify data in SimpleDB
				string userJson = await response.GetResponseTextAsync();
				TextResponse = userJson;
				//dataSource = JsonConvert.DeserializeObject<DataSourceRoot>(userJson);
			}


			// var response = await ProviderService.GetGoogleAsync("https://www.googleapis.com/fitness/v1/users/me/dataSources");


			//if (response == null)
			//{
			//    return;
			//}

			CanRequest = true;

		}

		Account account;
		//AccountStore store;

		async void OnAuthCompleted(object sender, AuthenticatorCompletedEventArgs e)
        {
            AuthState = "Auth Sucessfull";

            var authenticator = sender as OAuth2Authenticator;
            if (authenticator != null)
            {
                authenticator.Completed -= OnAuthCompleted;
                authenticator.Error -= OnAuthError;
            }

			if (e.IsAuthenticated)
			{
				//await store.SaveAsync(account, AppConstant.Constants.AppName);

				try
				{
					account = e.Account;
					await SecureStorage.SetAsync(Constants.GoogleData, JsonConvert.SerializeObject(e.Account.Properties));
				}
				catch (Exception ex)
				{
					Debug.WriteLine(ex.Message);
				}

				AuthSuccess = CanRequest = true;

			}
		}

		void OnAuthError(object sender, AuthenticatorErrorEventArgs e)
		{

			AuthState = "Auth Error";

			var authenticator = sender as OAuth2Authenticator;
			if (authenticator != null)
			{
				authenticator.Completed -= OnAuthCompleted;
				authenticator.Error -= OnAuthError;
			}

			Debug.WriteLine("Authentication error: " + e.Message);
		}


	}
}