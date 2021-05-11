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
        public static void WriteCellValue(string value, int row, string letter, Worksheet sheet)
        {

            int column = ExcelColumnNameToNumber(letter);
            if (column > 100)
                return;

            sheet.Cells[row, column].Value2 = value;
            return;
        }

        public static void WriteCellValue(float value, int row, string letter, Worksheet sheet)
        {

            int column = ExcelColumnNameToNumber(letter);
            if (column > 100)
                return;

            sheet.Cells[row, column].Value2 = value;
            return;
        }

        public static string GetCellValue(int row, string letter, Worksheet sheet)
        {

            int column = ExcelColumnNameToNumber(letter);
            if (column > 100)
                return null;

            var value = sheet.Cells[row, column].Value2;
            return value != null ? value.ToString() : null;
        }

        public static string GetCellValue(int row, int column, Worksheet sheet)
        {
            if (column > 100)
                return null;

            var value = sheet.Cells[row, column].Value2;
            return value != null ? value.ToString() : null;
        }

        public static float GetCellValueFloat(int row, int column, Worksheet sheet)
        {
            if (column > 100)
                return 0;

            var value = sheet.Cells[row, column].Value2;
            return value != null ? (float)value : 0;
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

        public static string GetColumnName(int index)
        {
            index = index - 1;
            const string letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

            var value = "";

            if (index >= letters.Length)
                value += letters[index / letters.Length - 1];

            value += letters[index % letters.Length];

            return value;
        }
    }
}
