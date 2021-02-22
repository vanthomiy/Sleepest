using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using RaleWears.Models.CustomerContent.Shop;
using RaleWears.ViewModels.CustomerContent.Shop;
using RaleWears.Views.CustomerContent.Payment;
using Xamarin.Forms;

namespace RaleWears.Views.CustomerContent.Shop
{
    public partial class ShopPage : ContentPage
    {
        ObservableCollection<string> source = new ObservableCollection<string>();
        public ShopPage()
        {
            InitializeComponent();
            BindingContext = new ShopViewModel(Navigation);
            source = new ObservableCollection<string>();
          

        }

        async   void MyCollection_SelectionChanged(System.Object sender, Xamarin.Forms.SelectionChangedEventArgs e)
        {

            if (((CollectionView)sender).SelectedItem == null)
                return;

            var item = ((CollectionView)sender).SelectedItem as HighlightedProduct;

            await Navigation.PushAsync(new ShopPageDescription(item));

            ((CollectionView)sender).SelectedItem = null;

        }

      async  void TapGestureRecognizer_Tapped(System.Object sender, System.EventArgs e)
        {
            await Navigation.PushAsync(new PaymentScreen());
        }

        private void TapGestureRecognizer_Tapped_1(object sender, EventArgs e)
        {
            var l = sender as Label;
            SetCategory(l);
        }

        private void SetCategory(Label label)
        {

            // set all the styles to deselected
            foreach (var item in CategoryGrid.Children)
            {
                if (item is Label)
                {
                    item.Style = (Style)Application.Current.Resources["CategoryHeaderStyle"];
                }
            }

            // set the selected items style
            label.Style = (Style)Application.Current.Resources["CategorySelectedHeaderStyle"];

            // move the selection bar to the tapped item
            SelectionIndicator.TranslateTo(label.X, 0, 250, Easing.CubicInOut);
            SelectionIndicator.WidthRequest = label.Width;

        }

        void TapGestureRecognizer_Tapped_2(System.Object sender, System.EventArgs e)
        {
            MessagingCenter.Send<ShopPage>(this, "OpenMenu");
        }     

    }
}
