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

		public string LabelState = "Click to login";

        public AboutViewModel()
        {
            Title = "About";
            LoginCommand = new Command(StartAuth);
            Request1Command = new Command(RequestData);

			store = AccountStore.Create();

		}

		public ICommand LoginCommand { get; }
        public ICommand Request1Command { get; }

        public void StartAuth()
        {
			LabelState = "Wait for response";
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

			//authenticator.Completed += OnAuthCompleted;
			//authenticator.Error += OnAuthError;

			//AuthenticationState.Authenticator = authenticator;

			//var presenter = new Xamarin.Auth.Presenters.OAuthLoginPresenter();
			//presenter.Login(authenticator);



			authenticator.Completed += OnAuthCompleted;
			authenticator.Error += OnAuthError;
			authenticator.IsLoadableRedirectUri = true;
			AuthenticationState.Authenticator = authenticator;

			var presenter = new Xamarin.Auth.Presenters.OAuthLoginPresenter();
			presenter.Login(authenticator);
		}

        public void RequestData()
        {



        }

		Account account;
		AccountStore store;


		async void OnAuthCompleted(object sender, AuthenticatorCompletedEventArgs e)
		{
			LabelState = "Success";

			var authenticator = sender as OAuth2Authenticator;
			if (authenticator != null)
			{
				authenticator.Completed -= OnAuthCompleted;
				authenticator.Error -= OnAuthError;
			}

			DataSourceRoot dataSource = null;
			if (e.IsAuthenticated)
			{
				// If the user is authenticated, request their basic user data from Google
				// UserInfoUrl = https://www.googleapis.com/oauth2/v2/userinfo
				var request = new OAuth2Request("GET", new Uri(AppConstant.Constants.SleepDataUrl), null, e.Account);
				var response = await request.GetResponseAsync();
				if (response != null)
				{
					// Deserialize the data and store it in the account store
					// The users email address will be used to identify data in SimpleDB
					string userJson = await response.GetResponseTextAsync();
					dataSource = JsonConvert.DeserializeObject<DataSourceRoot>(userJson);
				}

				if (dataSource != null)
				{
					//App.Current.MainPage = new NavigationPage(new MyDashBoardPage());
					LabelState = "Data Retrived";
				}

				//await store.SaveAsync(account = e.Account, AppConstant.Constants.AppName);
				//await DisplayAlert("Email address", user.Email, "OK");
			}
		}

		void OnAuthError(object sender, AuthenticatorErrorEventArgs e)
		{

			LabelState = "Error";

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