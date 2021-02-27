using System;
using RaleWears.Views.DashBoard;
using RaleWears.Views.Login;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;


namespace RaleWears
{
    public partial class App : Application
    {
        public App()
        {
            InitializeComponent();
            MainPage = new  NavigationPage(new LoginPage());
        }

        protected override void OnStart()
        {
        }

        protected override void OnSleep()
        {
        }

        protected override void OnResume()
        {
        }
    }
}
