using Newtonsoft.Json;
using System;
using Xamarin.Auth;

namespace SleepestTest1.Authentification.Models
{
    [Serializable]
    public class RefreshResponse
    {
        [JsonProperty("access_token")]
        public string AccessToken { get; set; }

        [JsonProperty("expires_in")]
        public int ExpiresIn { get; set; }

        [JsonProperty("scope")]
        public string Scope { get; set; }

        [JsonProperty("token_type")]
        public string TokenType { get; set; }

    }

    [Serializable]
    public class RefreshRequest
    {
        [JsonProperty("refresh_token")]
        public string RefreshToken { get; set; }

        [JsonProperty("client_id")]
        public string ClientId { get; set; }

        [JsonProperty("grant_type")]
        public string GrantType { get; set; }

        public RefreshRequest(Account googleAccount)
        {

            var googleTokenString = JsonConvert.SerializeObject(googleAccount.Properties);
            var refreshToken = JsonConvert.DeserializeObject<GoogleToken>(googleTokenString).RefreshToken;

            RefreshToken = refreshToken;
            ClientId = AppConstant.Constants.AndroidClientId;
            GrantType = "refresh_token";
        }

    }
}
