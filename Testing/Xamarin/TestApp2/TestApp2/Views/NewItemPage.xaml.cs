using System;
using System.Collections.Generic;
using System.ComponentModel;
using TestApp2.Models;
using TestApp2.ViewModels;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace TestApp2.Views
{
    public partial class NewItemPage : ContentPage
    {
        public Item Item { get; set; }

        public NewItemPage()
        {
            InitializeComponent();
            BindingContext = new NewItemViewModel();
        }
    }
}