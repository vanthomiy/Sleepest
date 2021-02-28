using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification;
using SleepestTest1.Authentification.Service;
using SleepestTest1.Authentification.Models;
using SleepestTest1.Models;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Windows.Input;
using Xamarin.Auth;
using Xamarin.Essentials;
using Xamarin.Forms;
using SleepestTest1.Authentification.Helper;
using SleepestTest1.FitnessData.Service;

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

			// Check for auth automaticaly after start
			StartAuth();
		}

		public ICommand LoginCommand { get; }
        public ICommand GetCommand { get; }
        public ICommand PostCommand { get; }

        public async void StartAuth()
        {
			AuthState = "Wait for response";

			//if (await AuthRenewal.CheckTokenAndRenewIfNeccessary())
			//{
			//	AuthState = "Authorized with refresh token";
			//	AuthSuccess = CanRequest = true;
			//	return;
			//}

			AuthentificationService.AuthRequest(this);

			//string clientId = AppConstant.Constants.AndroidClientId;
			//string redirectUri = AppConstant.Constants.AndroidRedirectUrl;

			//var authenticator = new OAuth2Authenticator(
			//	clientId,
			//	null,
			//	AppConstant.Constants.Scope,
			//	new Uri(AppConstant.Constants.AuthorizeUrl),
			//	new Uri(redirectUri),
			//	new Uri(AppConstant.Constants.AccessTokenUrl),
			//	null,
			//	true);

			//authenticator.Completed += OnAuthCompleted;
			//authenticator.Error += OnAuthError;
			//authenticator.IsLoadableRedirectUri = true;
			//AuthenticationState.Authenticator = authenticator;

			//var presenter = new Xamarin.Auth.Presenters.OAuthLoginPresenter();
			//presenter.Login(authenticator);


		}

		public async void GetRequest()
        {
			CanRequest = false;
            // If the user is authenticated, request their basic user data from Google
           
            var response = await ProviderService.GetGoogleAsync(RequestUrl);

            if (response != null)
            {
				TextResponse = response;
			}

			CanRequest = true;
		}

		public async void PostRequest()
		{
			CanRequest = false;

            // If the user is authenticated, request their basic user data from Google

			//Request req = RequestBuilder.
			//var response = await ProviderService.PostGoogleAsync(url, reqBody); //Convert response to json class


    //        if (response != null) 
    //        {
				//TextResponse = response;
				//string puffer = Session.convertJson(response);
    //        }

            CanRequest = true;
		}

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

				try
				{
					await SecureStorage.SetAsync(Constants.GoogleAccount, JsonConvert.SerializeObject(e.Account));

					var googleToken = await DirectConvert.GetGoogleToken();
					var expiresIn = googleToken.ExpiresIn;
					var tokenExpires = DateTime.Now.AddSeconds(Convert.ToInt32(expiresIn));

					// Save tokenExpires
					await SecureStorage.SetAsync(Constants.GoogleTokenExpires, JsonConvert.SerializeObject(tokenExpires));

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