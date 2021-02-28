using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification.Helper;
using SleepestTest1.Authentification.Models;
using SleepestTest1.ViewModels;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Auth;
using Xamarin.Essentials;

namespace SleepestTest1.Authentification.Service
{
    public class AuthentificationService
    {
        /// <summary>
        /// Renew the actual google account and retrive a new access token 
        /// </summary>
        /// <param name="googleAccount">The actual account that was used to login to google api</param>
        /// <returns>The Refresh Repsonse which contains a new access token</returns>
        public static async Task<RefreshResponse> RefreshTokenRequest(Account googleAccount)
        {
            RefreshRequest request = new RefreshRequest(googleAccount);

            DirectConvert<RefreshRequest> dc = new DirectConvert<RefreshRequest>();
            var tokenRefreshRequestDict = dc.ClassToDictionary(request);

            var refreshRequest = new OAuth2Request("POST", new Uri(Constants.RefreshUrl), tokenRefreshRequestDict, googleAccount);

            RefreshResponse response = null;

            await refreshRequest.GetResponseAsync().ContinueWith(task => {
                if (task.IsFaulted)
                {
                    Console.WriteLine("Error: " + task.Exception.InnerException.Message);
                }
                else
                {
                    try
                    {
                        var jsonResponse = task.Result.GetResponseText();
                        response = JsonConvert.DeserializeObject<RefreshResponse>(jsonResponse);
                    }
                    catch (Exception exception)
                    {
                        Console.WriteLine("!!!!!Exception: {0}", exception.ToString());
                    }
                }
            });

            return response;
        }


        public static void AuthRequest(Object view)
        {
            string clientId = Constants.AndroidClientId;
            string redirectUri = Constants.AndroidRedirectUrl;

            var authenticator = new OAuth2Authenticator(
                clientId,
                null,
                Constants.Scope,
                new Uri(Constants.AuthorizeUrl),
                new Uri(redirectUri),
                new Uri(Constants.AccessTokenUrl),
                null,
                true);

            var viewModel = view as AboutViewModel;

            authenticator.Completed += async (object sender, AuthenticatorCompletedEventArgs e) =>
            {
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

                        viewModel.AuthState = "Auth Success";
                        viewModel.AuthSuccess = viewModel.CanRequest = true;

                    }
                    catch (Exception ex)
                    {
                        Debug.WriteLine("Authentication error: " + ex.Message);
                        viewModel.AuthState = "Auth Error";
                    }

                }

                viewModel.AuthState = "Not authentificated";

            };

            authenticator.Error += (object sender, AuthenticatorErrorEventArgs e) =>
            {
                Debug.WriteLine("Authentication error: " + e.Message);
                viewModel.AuthState = "Auth Error";
            };

            authenticator.IsLoadableRedirectUri = true;
            AuthenticationState.Authenticator = authenticator;

            var presenter = new Xamarin.Auth.Presenters.OAuthLoginPresenter();
            presenter.Login(authenticator);
        }
    }
}
