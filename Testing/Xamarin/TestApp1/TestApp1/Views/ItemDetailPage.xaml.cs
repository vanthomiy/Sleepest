using System.ComponentModel;
using TestApp1.ViewModels;
using Xamarin.Forms;

namespace TestApp1.Views
{
    public partial class ItemDetailPage : ContentPage
    {
        public ItemDetailPage()
        {
            InitializeComponent();
            BindingContext = new ItemDetailViewModel();
        }
    }
}