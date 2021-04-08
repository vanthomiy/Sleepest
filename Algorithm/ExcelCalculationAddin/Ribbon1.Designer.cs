
namespace ExcelCalculationAddin
{
    partial class Ribbon1 : Microsoft.Office.Tools.Ribbon.RibbonBase
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        public Ribbon1()
            : base(Globals.Factory.GetRibbonFactory())
        {
            InitializeComponent();
        }

        /// <summary> 
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">"true", wenn verwaltete Ressourcen gelöscht werden sollen, andernfalls "false".</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Komponenten-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.tab1 = this.Factory.CreateRibbonTab();
            this.group1 = this.Factory.CreateRibbonGroup();
            this.btnCalcLive = this.Factory.CreateRibbonButton();
            this.btnEinlesen = this.Factory.CreateRibbonButton();
            this.btnCalculieren = this.Factory.CreateRibbonButton();
            this.btnJsonExport = this.Factory.CreateRibbonButton();
            this.tab1.SuspendLayout();
            this.group1.SuspendLayout();
            this.SuspendLayout();
            // 
            // tab1
            // 
            this.tab1.ControlId.ControlIdType = Microsoft.Office.Tools.Ribbon.RibbonControlIdType.Office;
            this.tab1.Groups.Add(this.group1);
            this.tab1.Label = "TabAddIns";
            this.tab1.Name = "tab1";
            // 
            // group1
            // 
            this.group1.Items.Add(this.btnJsonExport);
            this.group1.Items.Add(this.btnCalcLive);
            this.group1.Items.Add(this.btnEinlesen);
            this.group1.Items.Add(this.btnCalculieren);
            this.group1.Label = "group1";
            this.group1.Name = "group1";
            // 
            // btnCalcLive
            // 
            this.btnCalcLive.Label = "Kalk Live";
            this.btnCalcLive.Name = "btnCalcLive";
            this.btnCalcLive.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.btnCalcLive_Click);
            // 
            // btnEinlesen
            // 
            this.btnEinlesen.Label = "Einlesen";
            this.btnEinlesen.Name = "btnEinlesen";
            this.btnEinlesen.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.btnEinlesen_Click);
            // 
            // btnCalculieren
            // 
            this.btnCalculieren.Label = "Kalkulieren";
            this.btnCalculieren.Name = "btnCalculieren";
            this.btnCalculieren.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.btnCalculieren_Click);
            // 
            // btnJsonExport
            // 
            this.btnJsonExport.Label = "JsonExport";
            this.btnJsonExport.Name = "btnJsonExport";
            this.btnJsonExport.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(this.btnJsonExport_Click);
            // 
            // Ribbon1
            // 
            this.Name = "Ribbon1";
            this.RibbonType = "Microsoft.Excel.Workbook";
            this.Tabs.Add(this.tab1);
            this.Load += new Microsoft.Office.Tools.Ribbon.RibbonUIEventHandler(this.Ribbon1_Load);
            this.tab1.ResumeLayout(false);
            this.tab1.PerformLayout();
            this.group1.ResumeLayout(false);
            this.group1.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        internal Microsoft.Office.Tools.Ribbon.RibbonTab tab1;
        internal Microsoft.Office.Tools.Ribbon.RibbonGroup group1;
        internal Microsoft.Office.Tools.Ribbon.RibbonButton btnCalculieren;
        internal Microsoft.Office.Tools.Ribbon.RibbonButton btnEinlesen;
        internal Microsoft.Office.Tools.Ribbon.RibbonButton btnCalcLive;
        internal Microsoft.Office.Tools.Ribbon.RibbonButton btnJsonExport;
    }

    partial class ThisRibbonCollection
    {
        internal Ribbon1 Ribbon1
        {
            get { return this.GetRibbon<Ribbon1>(); }
        }
    }
}
