using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.ListHelp
{
    public static class CellHelper
    {
        public static string GetCellValue(int row, string letter, Worksheet sheet)
        {

            int column = ExcelColumnNameToNumber(letter);
            if (column > 40)
                return null;

            var value = sheet.Cells[row, column].Value2;
            return value != null ? value.ToString() : null;
        }

        public static string GetCellValue(int row, int column, Worksheet sheet)
        {
            if (column > 40)
                return null;

            var value = sheet.Cells[row, column].Value2;
            return value != null ? value.ToString() : null;
        }

        public static int ExcelColumnNameToNumber(string columnName)
        {
            if (string.IsNullOrEmpty(columnName)) throw new ArgumentNullException("columnName");

            columnName = columnName.ToUpperInvariant();

            int sum = 0;

            for (int i = 0; i < columnName.Length; i++)
            {
                sum *= 26;
                sum += (columnName[i] - 'A' + 1);
            }

            return sum;
        }
    }
}
