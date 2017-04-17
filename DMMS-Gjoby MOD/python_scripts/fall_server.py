import time, socket, json, struct, sys, signal
from math import pow, sqrt

# Check if the necessary arguments are provided
if (len(sys.argv) < 3):
    print "Usage: %s <IP-address> <Port>"%sys.argv[0]
    exit(1)

# Read input arguments
TCP_IP = sys.argv[1]
TCP_PORT = int(sys.argv[2])

# Specifies IP-address and port
s = socket.socket(socket.AF_INET,
        socket.SOCK_STREAM)
s.bind((TCP_IP, TCP_PORT))

# Blocks and waits for a connection
s.listen(1)
clientsocket, address = s.accept()

# Connected
print "Server started and listening.."
print "accepted.."

# Server timeouts if it does not receive a message for 10 seconds
start = time.time()
clientsocket.settimeout(10)

i = 0
free_fall = False
data_string = ""
try:
    # While a message is received within the last 10 seconds
    while True:
        # Receive a message up to 
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
                j_data = json.loads(lines)
                if j_data['type'] == "data":
                    # Calculate the length of the euclidean vector
                    euc_len = 0.0
                    for elem in j_data['data']:
                        euc_len += pow(elem['value'], 2)
                    euc_len = sqrt(euc_len)

                    # Phone in free fall
                    if euc_len <= 6.0:
                        free_fall = True
                        i = 0

                    # Freefall in one of the 5 last messsages
                    if free_fall == True:
                        i+=1
                        # Check if phone has stopped falling
                        if euc_len >= 13.5:
                            i = 0
                            free_fall = False
                            print "FALL"

                    # End of sliding window
                    if i > 4:
                        i = 0
                        free_fall = False

except socket.timeout:
    # No message received in the last 10 seconds, closing connection
    clientsocket.shutdown(socket.SHUT_RDWR)
    clientsocket.close()
    s.shutdown(socket.SHUT_RDWR)
    s.close()

