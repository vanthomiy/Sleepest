using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification.Models;
using SleepestTest1.Authentification.Service;
using System;
using System.Threading.Tasks;
using Xamarin.Auth;
using Xamarin.Essentials;

namespace SleepestTest1.Authentification
{
    public class AuthRenewal
    {
        // defines the time the token expires
        private static DateTime tokenExpires = DateTime.MinValue;

        /// <summary>
        /// Check this before a request is send
        /// It Checks: 
        /// Is account available else false
        /// Is token valid else it renews the token... success = true
        /// </summary>
        /// <returns>True when token available and not expired or successfully renewed</returns>
        public static async Task<bool> CheckTokenAndRenewIfNeccessary()
        {
            string googleAccountString = await SecureStorage.GetAsync(Constants.GoogleAccount);
            if (googleAccountString != null)
            {
                var googleAccount = JsonConvert.DeserializeObject<Account>(googleAccountString);

                // if tokenExpires not set check from secure storage and assign the old value
                if (tokenExpires == DateTime.MinValue)
                {
                   var tokenExpiresString = await SecureStorage.GetAsync(Constants.GoogleTokenExpires);
                    tokenExpires = tokenExpiresString != null ? JsonConvert.DeserializeObject<DateTime>(tokenExpiresString) : tokenExpires;
                }

                // if access token expires in more than 5 minutes dont renew it
                if (tokenExpires.Subtract(DateTime.Now) > TimeSpan.FromMinutes(5))
                {
                    return true;
                }

                var response = await AuthentificationService.RefreshTokenRequest(googleAccount);

                if (response != null && response.AccessToken != null && response.AccessToken != "")
                {
                    // Update tokenExpires
                    tokenExpires = DateTime.Now.AddSeconds(response.ExpiresIn);
                    await SecureStorage.SetAsync(Constants.GoogleTokenExpires, JsonConvert.SerializeObject(tokenExpires));

                    // Update token of the account  ?? acces_token ???
                    googleAccount.Properties["access_token"] = response.AccessToken;

                    // Save updated account
                    await SecureStorage.SetAsync(Constants.GoogleAccount, JsonConvert.SerializeObject(googleAccount));

                    return true;
                }
            }
            // no new token could be retrived
            return false;
        }
    }
}
