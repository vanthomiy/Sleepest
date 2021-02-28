using Newtonsoft.Json;
using SleepestTest1.AppConstant;
using SleepestTest1.Authentification.Models;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Auth;
using Xamarin.Essentials;

namespace SleepestTest1.Authentification.Helper
{
    /// <summary>
    /// Helper class to Convert diffrent types of files for the Auth process
    /// Dict to Class or Class to Dict
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class DirectConvert<T>
    {
        public T DictionaryToClass(Dictionary<string,string> dict)
        {
            try
            {
                var dictJson = JsonConvert.SerializeObject(dict);
                var classObject = JsonConvert.DeserializeObject<T>(dictJson);
                return classObject;
            }
            catch (Exception)
            {
                return default;
            }
        }

        public Dictionary<string, string> ClassToDictionary(T myClass)
        {
            try
            {
                var classJson = JsonConvert.SerializeObject(myClass);
                var dictObjecr = JsonConvert.DeserializeObject<Dictionary<string, string>>(classJson);
                return dictObjecr;
            }
            catch (Exception)
            {
                return null;
            }
        }

    }

    public class DirectConvert
    {
        /// <summary>
        /// Retrives and modifies the account from Secure Storage to get a googletoken class back from it
        /// </summary>
        /// <returns>Returns the Google token of the stored account details in the secure storage</returns>
        public static async Task<GoogleToken> GetGoogleToken()
        {
            string googleAccountString = await SecureStorage.GetAsync(Constants.GoogleAccount);
            var account = JsonConvert.DeserializeObject<Account>(googleAccountString);
            var googleTokenString = JsonConvert.SerializeObject(account.Properties);
            return JsonConvert.DeserializeObject<GoogleToken>(googleTokenString);
        }
    }
}
