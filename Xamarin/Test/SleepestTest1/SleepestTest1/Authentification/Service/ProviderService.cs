using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification.Tokens;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Essentials;

namespace SleepestTest1.Authentification.Service
{
    public static class ProviderService
    {
        public static async Task<string> GetGoogleAsync(string url)
        {
            string googleTokenString = await SecureStorage.GetAsync(Constants.GoogleData);
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

        public static async Task<string> PostGoogleAsync(string url)
        {
            string googleTokenString = await SecureStorage.GetAsync(Constants.GoogleData);
            string googleToken = JsonConvert.DeserializeObject<GoogleToken>(googleTokenString).AccessToken;

            var authHeader = new AuthenticationHeaderValue("Bearer", googleToken);

            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.Authorization = authHeader;
            var content = new StringContent("{\"aggregateBy\":[{\"dataTypeName\":\"com.google.sleep.segment\"}],\"endTimeMillis\":1575609060000,\"startTimeMillis\":1575591360000}", Encoding.UTF8, "application/json");
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
