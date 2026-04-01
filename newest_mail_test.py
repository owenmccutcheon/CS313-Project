import socket
import threading

def try_mail(thread_id):
    try:
        s = socket.socket()
        s.connect(("localhost", 5000))

        name = f"user{thread_id}"
        s.sendall(f"/name {name}\n".encode())

        # send MANY messages
        for i in range(100):
            s.sendall(f"/mail alice {name}-msg-{i}\n".encode())

        s.close()
    except Exception as e:
        print("Error:", e)

threads = []

# 🔥 MORE THREADS
for i in range(50):
    t = threading.Thread(target=try_mail, args=(i,))
    threads.append(t)
    t.start()

for t in threads:
    t.join()