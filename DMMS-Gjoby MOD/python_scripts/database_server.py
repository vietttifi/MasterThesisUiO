import socket, json, signal, sys, time
import sqlite3 as lite
from sensorlib import *

def save_metadata(data):
    wrapper_id = int(data['id'])
    wrapper_name[driver_id] = data['name']
    for channel in data['channels']:
        channel_id = int(channel['id'])
        channel_type = channel['type']
        channel_metric = channel['metric']
        channel_descr = channel['description']

        meta_type[wrapper_id][channel_id] = channel_type
        meta_metric[wrapper_id][channel_id] = channel_metric
        meta_descr[wrapper_id][channel_id] = channel_descr

def save_data(data):
    w_id = int(data['id'])
    time = data['time']
    for elem in data['data']:
        c_id = int(elem['id'])
        insert_data(cur, w_id, driver_name[w_id], time,
                c_id, elem['value'],
                meta_type[w_id][c_id], meta_metric[w_id][c_id],
                meta_descr[w_id][c_id])

def signal_handler(signal, frame):
    print('You pressed Ctrl+C!')
    clientsocket.close()
    s.close()
    con.commit()
    sys.exit(0)

# Create matrixes
wrapper_name = [[''] for k in range(10)]
meta_type = create_string_matrix(10,10)
meta_metric = create_string_matrix(10,10)
meta_descr = create_string_matrix(10,10)

# Open database
con = lite.connect('sensordata.db')
cur = open_data(con)

if (len(sys.argv) < 3):
    print "Usage: %s <IP-address> <Port>"%sys.argv[0]
    exit(1)


# Read commandline arguments
TCP_IP = sys.argv[1]
TCP_PORT = int(sys.argv[2])

# Specify IP-address and port of the TCP socket
s = socket.socket(socket.AF_INET,
        socket.SOCK_STREAM)
s.bind((TCP_IP, TCP_PORT))

#Blocks and listens for a conection 
s.listen(1)
print "Server started and listening.."
clientsocket, address = s.accept()

# Connected
print "Accepted connection from %s" %address
start = time.time()
clientsocket.settimeout(30) # Set timeout
signal.signal(signal.SIGINT, signal_handler)

data_string = ""
try:
    while True:
        data = clientsocket.recv(10000)
        data_len = len(data)
        if data_len > 0:
            # Append to previous incomplete msg
            data_string += data.decode()
            # The data is newline separated
            data_split = data_string.split('\n') 
            # Save the last element, possible incomplete msg
            data_string = data_split[-1]

            for line in data_split[:-1]:
                json_data = json.loads(line)
                if json_data['type'] == "meta":
                    save_metadata(json_data)
                if json_data['type'] == "data":
                    save_data(json_data)

except socket.timeout:
    clientsocket.shutdown(socket.SHUT_RDWR)
    clientsocket.close()
    s.shutdown(socket.SHUT_RDWR)
    s.close()


con.commit()
print count
