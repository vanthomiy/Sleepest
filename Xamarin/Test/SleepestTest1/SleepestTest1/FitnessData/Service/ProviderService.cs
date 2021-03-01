using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification;
using SleepestTest1.Authentification.Helper;
using SleepestTest1.Authentification.Models;
using SleepestTest1.FitnessData.Models;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Auth;
using Xamarin.Essentials;

namespace SleepestTest1.FitnessData.Service
{
    public static class ProviderService
    {

        /// <summary>
        /// Send a google GET request to a specific URL
        /// </summary>
        /// <param name="url">The url to get information from</param>
        /// <returns>A json response as string</returns>
        public static async Task<string> GetGoogleAsync(string url)
        {
            if (!await AuthRenewal.CheckTokenAndRenewIfNeccessary())
            {
                // Add some logic here to check if internet connection is lost or we have to reauthentificate
                return null;
            }

            var googleToken = await DirectConvert.GetGoogleToken();
            var authHeader = new AuthenticationHeaderValue("Bearer", googleToken.AccessToken);

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
        public static async Task<string> PostGoogleAsync(FitRequest req)
        {
            if (!await AuthRenewal.CheckTokenAndRenewIfNeccessary())
            {
                // Add some logic here to check if internet connection is lost or we have to reauthentificate
                return null;
            }

            var googleToken = await DirectConvert.GetGoogleToken();
            var authHeader = new AuthenticationHeaderValue("Bearer", googleToken.AccessToken);

            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Authorization = authHeader;
            var content = new StringContent(req.requestBody, Encoding.UTF8, "application/json");

            HttpResponseMessage httpResponse = await httpClient.PostAsync(req.uri, content);

            if (!httpResponse.IsSuccessStatusCode)
            {
                Debug.WriteLine($"Could not get GOOGLE email. Status: {httpResponse.StatusCode}");
            }

            string data = await httpResponse.Content.ReadAsStringAsync();

            return await Task.FromResult(data);
        }
    }
}
