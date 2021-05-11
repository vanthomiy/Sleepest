import csv

def LoadCsv(isCheck):
    with open('C:\\Users\\vanth\\OneDrive\\Studium\\6. Semester\\Projektarbeit\\02_Planung\\02_Algorithmus\\Parameter\\ThomasCSV\\Thomas1615474500.csv', 'r',encoding='utf-8-sig') as file:
        reader = csv.reader(file, )
        a = 0 
        headers = []
        mysuperdict = []
        for row in reader:
            if a == 0:
                a += 1
                headers = row
            else:
                mydict = {}
                i = 0
                for text in row:
                    if (i != 1 or isCheck):
                        mydict[headers[i]] = text         
                    i+=1

                mysuperdict.append(mydict)

        return mysuperdict        
    
