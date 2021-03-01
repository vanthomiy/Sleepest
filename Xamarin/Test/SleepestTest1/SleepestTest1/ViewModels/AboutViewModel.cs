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
using Google.Apis.Requests;
using SleepestTest1.FitnessData.Builder;
using SleepestTest1.FitnessData.Models;

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

            if (await AuthRenewal.CheckTokenAndRenewIfNeccessary())
            {
                AuthState = "Authorized with refresh token";
                AuthSuccess = CanRequest = true;
                return;
            }

			// Auth request in extra Service class... no listeners needed anymore
			// just provide from where you access it "this"
            AuthentificationService.AuthRequest(this);
		}

		public async void GetRequest()
        {
			CanRequest = false;
            // If the user is authenticated, request their basic user data from Google
           
            var response = await ProviderService.GetGoogleAsync(RequestUrl);

            if (response != null)
            {
				TextResponse = response;
				var dataSources = JsonConvert.DeserializeObject<DataSources>(response);
			}

			CanRequest = true;
		}

		public async void PostRequest()
		{
			CanRequest = false;

			RequestBuilder rb = new RequestBuilder();

			var a = new Google.Apis.Fitness.v1.FitnessService.Initializer();

			
			
			// Request builder helps to create specific requests
			DataSourcesRequest dsr = new DataSourcesRequest(FitRequestBuilder<DataSourcesRequest>.RequestDataType[DataSourceType.SleepSegments], DateTimeOffset.Now.AddDays(-20));
			var req = FitRequestBuilder<DataSourcesRequest>.CreateRequest(RequestType.AllDataSources, dsr);
			var response = await ProviderService.PostGoogleAsync(req); //Convert response to json class

            if (response != null)
            {
                TextResponse = response;
				var dataSources = JsonConvert.DeserializeObject<Session>(response);
            }

            CanRequest = true;
		}
	}
}