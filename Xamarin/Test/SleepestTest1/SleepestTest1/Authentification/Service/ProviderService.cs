using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification.Models;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Auth;
using Xamarin.Essentials;

namespace SleepestTest1.Authentification.Service
{
    public static class ProviderService
    {
        /// <summary>
        /// Renew the actual google account and retrive a new access token 
        /// </summary>
        /// <param name="googleAccount">The actual account that was used to login to google api</param>
        /// <returns>The Refresh Repsonse which contains a new access token</returns>
        public static async Task<RefreshResponse> RefreshTokenRequest(Account googleAccount)
        {
            RefreshRequest request = new RefreshRequest(googleAccount);
            var tokenRefreshRequestString = JsonConvert.SerializeObject(request);
            var tokenRefreshRequestDict = JsonConvert.DeserializeObject<Dictionary<string, string>>(tokenRefreshRequestString);

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

        /// <summary>
        /// Send a google GET request to a specific URL
        /// </summary>
        /// <param name="url">The url to get information from</param>
        /// <returns>A json response as string</returns>
        public static async Task<string> GetGoogleAsync(string url)
        {
            if (!await AuthRenewal.CheckTokenAndRenewIfNeccessary())
            {
                // wild
   
            }

            string googleAccountString = await SecureStorage.GetAsync(Constants.GoogleAccount);
            var account = JsonConvert.DeserializeObject<Account>(googleAccountString);
            var googleTokenString = JsonConvert.SerializeObject(account.Properties);
            string googleToken = JsonConvert.DeserializeObject<GoogleToken>(googleTokenString).AccessToken;
            var authHeader = new AuthenticationHeaderValue("Bearer", googleToken);

            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Authorization = authHeader;

            HttpResponseMessage httpResponse = await httpClient.GetAsync($"{url}");


            if (!httpResponse.IsSuccessStatusCode)
            {
                Debug.WriteLine($"Could not get GOOGLE email. Status: {httpResponse.StatusCode}");
            }

            string data = await httpResponse.Content.ReadAsStringAsync();

            return await Task.FromResult(data);
        }

        /// <summary>
        /// Send a google POST request to a specifc URL with a specific JSON Body
        /// </summary>
        /// <param name="url">The url to get information from</param>
        /// <param name="requestBody">A json as string that contains the body</param>
        /// <returns>A json response as string</returns>
        public static async Task<string> PostGoogleAsync(string url, string requestBody)
        {
            string googleAccountString = await SecureStorage.GetAsync(Constants.GoogleAccount);
            var account = JsonConvert.DeserializeObject<Account>(googleAccountString);

            var googleTokenString = JsonConvert.SerializeObject(account.Properties);
            string googleToken = JsonConvert.DeserializeObject<GoogleToken>(googleTokenString).AccessToken;

            var authHeader = new AuthenticationHeaderValue("Bearer", googleToken);

            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Authorization = authHeader;
            var content = new StringContent(requestBody, Encoding.UTF8, "application/json");

            HttpResponseMessage httpResponse = await httpClient.PostAsync($"{url}", content);

            if (!httpResponse.IsSuccessStatusCode)
            {
                Debug.WriteLine($"Could not get GOOGLE email. Status: {httpResponse.StatusCode}");
            }

            string data = await httpResponse.Content.ReadAsStringAsync();

            return await Task.FromResult(data);
        }
    }
}
