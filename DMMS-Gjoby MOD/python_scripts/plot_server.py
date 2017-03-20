import socket, json, time, signal, sys
import plotly.plotly as py
from random import randint
from plotly.graph_objs import *

def signal_handler(signal, frame):
    print('You pressed Ctrl+C!')
    clientsocket.close()
    s.close()
    sys.exit(0)

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

# Blocks and listens for a conection 
s.listen(1)
print "Server started and listening.."
clientsocket, address = s.accept()

# Connected
print "Accepted connection from %s" %address
start = time.time()
clientsocket.settimeout(30) # Set timeout
signal.signal(signal.SIGINT, signal_handler)

outfile = open('shared_file.txt', 'w')
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
                if json_data['type'] == "data":
                    # Create string and write it to file
                    string = ""
                    for elem in json_data['data']:
                        string = "%f "%elem['value']

                    outfile.write(string +"\n")
                    outfile.flush()


except socket.timeout:
    # No message received in the last 10 seconds, closing connection
    clientsocket.shutdown(socket.SHUT_RDWR)
    clientsocket.close()
    s.shutdown(socket.SHUT_RDWR)
    s.close()
    outfile.close()
