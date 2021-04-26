using Microsoft.WindowsAPICodePack.Dialogs;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Export
{
    public class ExportFile
    {

        public static Task<string> GetFolder()
        {
            CommonOpenFileDialog dialog = new CommonOpenFileDialog();

            dialog.IsFolderPicker = true;

            if (dialog.ShowDialog() == CommonFileDialogResult.Ok)
            {
                return Task.FromResult(dialog.FileName);
            }

            return null;

        }

        public static void Export(string file, string name, string path)

        {

            string exportname = path + @"\" + name + ".json";

            FileStream fs = new FileStream(exportname, FileMode.Create, FileAccess.Write);

            StreamWriter write = new StreamWriter(fs, Encoding.UTF8);
            write.Write(file);
            write.Close();
               
        }

        public static void ExportCSV(string file, string name, string path, string folder)
        {
            string exportname = path + $@"\{folder}\" + name + ".csv";

            FileStream fs = new FileStream(exportname, FileMode.Create, FileAccess.Write);

            StreamWriter write = new StreamWriter(fs, Encoding.UTF8);
            write.Write(file);
            write.Close();
        }


        public static void ExportJSON(string file, string name, string path)
        {

            string exportname = path + @"\JsonFiles\" + name + ".json";

            FileStream fs = new FileStream(exportname, FileMode.Create, FileAccess.Write);

            StreamWriter write = new StreamWriter(fs, Encoding.UTF8);
            write.Write(file);
            write.Close();
        }


    }
}
