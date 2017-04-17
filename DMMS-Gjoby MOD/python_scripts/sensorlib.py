import sys

def open_data(con):
    cur = con.cursor()
    try:
        cur.execute("CREATE TABLE SensorData(DriverID INT, WrapperName TEXT,Timestamp \
                TEXT, ChannelId INT, Value FLOAT, Datatype TEXT, Metric TEXT, Description TEXT)")
    except:
        print "Allready created"
    return cur

def insert_data(cur, w_id, w_name, time, c_id, value, datatype, metric, des):
    cur.execute("INSERT INTO SensorData
            VALUES(%d,'%s','%s',%d,%f,'%s','%s','%s')"%(w_id, w_name, time, c_id, value, datatype, metric, des))

def create_string_matrix(x, y):
    matrix =[['']*y for x in range(x)]
    return matrix

def print_meta(matrix, tabs):
    x = len(matrix)
    y = len(matrix[0][:])
    sys.stdout.write("\t")
    for i in range(x):
        sys.stdout.write("|%d%s" %(i, "\t"*tabs))
    sys.stdout.write("\n")
    sys.stdout.write("%s"%"-"*8*(10*tabs +1))
    sys.stdout.write("\n")

    for i in range(x):
        sys.stdout.write("|%d%s"%(i, "\t"))
        for j in range(y):
            sys.stdout.write("|%s%s"
                    %(matrix[i][j],"\t"*(tabs-((len(matrix[i][j])+1)/8))))
        sys.stdout.write("\n")
    sys.stdout.write("%s"%"-"*8*(10*tabs +1))
    sys.stdout.write("\n")
