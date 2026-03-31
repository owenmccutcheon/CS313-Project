from socket import *
import sys
import time

if len(sys.argv) != 3:
    print("Usage: python python_chat.py <host> <port>")
    sys.exit()

host = sys.argv[1]
port = int(sys.argv[2])

sock = socket(AF_INET, SOCK_STREAM)
sock.connect((host, port))

sock.sendall(("/name test\n").encode("utf-8"))

for i in range(200):
    sock.sendall(("/join group1\n").encode("utf-8"))
    sock.sendall((f"msg {i}\n").encode("utf-8"))
    time.sleep(0.001)

sock.close()