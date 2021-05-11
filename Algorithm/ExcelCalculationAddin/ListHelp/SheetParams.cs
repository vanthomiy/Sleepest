using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.ListHelp
{
    public static class SheetParams
    {
        public static int GetRowsCount(Worksheet worksheet)
        {
            return worksheet.UsedRange.Rows.Count;
        }

        public static int GetColumnCount(Worksheet worksheet)
        {
            return worksheet.UsedRange.Columns.Count;
        }
    }
}
