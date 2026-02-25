from socket import *
import threading
import sys

if len(sys.argv) != 3:
    print("Usage: python python_chat.py <host> <port>")
    sys.exit()

host = sys.argv[1]
port = int(sys.argv[2])

sock = socket(AF_INET, SOCK_STREAM)
sock.connect((host, port))

running = True

def receive():
    global running
    try:
        while running:
            data = sock.recv(5000)
            if not data:
                break
            print("Peer:", data.decode("utf-8"), end="")
    except:
        pass
    finally:
        running = False

threading.Thread(target=receive, daemon=True).start()

try:
    while running:
        message = input("You: ")
        if message.lower() == "/quit":
            break
        sock.sendall((message + "\n").encode("utf-8"))
except KeyboardInterrupt:
    pass

sock.close()