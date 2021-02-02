using System;
using System.Windows.Input;
using Xamarin.Essentials;
using Xamarin.Forms;

namespace TestApp1.ViewModels
{
    public class AboutViewModel : BaseViewModel
    {
        public AboutViewModel()
        {
            Title = "About";
            OpenWebCommand = new Command(async () => await Browser.OpenAsync("https://aka.ms/xamarin-quickstart"));
            OpenWebCommand = new Command(OnTimerStart);
        }

        private void OnTimerStart(object obj)
        {
            Device.StartTimer(TimeSpan.FromSeconds(30), () =>
            {
                // Do something

                return true; // True = Repeat again, False = Stop the timer
            });
        }

        public ICommand OpenWebCommand { get; }

        public ICommand StartTimerCommand { get; }



    }
}