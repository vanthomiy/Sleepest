﻿using Microsoft.Office.Tools.Ribbon;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ExcelCalculationAddin
{
    public partial class Ribbon1
    {
        private void Ribbon1_Load(object sender, RibbonUIEventArgs e)
        {

        }

        private async void btnCalculieren_Click(object sender, RibbonControlEventArgs e)
        {
            // Kalkulieren und abspeichern der daten
            ReadParameter.GetAlarmSettings();

            ReadParameter.ReadSleepTypeParameter();



        }

        private async void btnEinlesen_Click(object sender, RibbonControlEventArgs e)
        {
            await ReadParameter.GetAllUserData();
        }
    }
}
