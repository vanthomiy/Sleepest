using System.ComponentModel;
using TestApp2.ViewModels;
using Xamarin.Forms;

namespace TestApp2.Views
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