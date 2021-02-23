using Newtonsoft.Json;
using SleepestTest1.Authentification;
using System;
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

			store = AccountStore.Create();

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

			account = store.FindAccountsForService(AppConstant.Constants.AppName).FirstOrDefault();

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

			//await store.SaveAsync(account = e.Account, AppConstant.Constants.AppName);
			//await DisplayAlert("Email address", user.Email, "OK");
			CanRequest = true;

		}

		public async void PostRequest()
		{
			CanRequest = false;

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

			CanRequest = true;

		}


		Account account;
		AccountStore store;


        void OnAuthCompleted(object sender, AuthenticatorCompletedEventArgs e)
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
                account = e.Account;
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