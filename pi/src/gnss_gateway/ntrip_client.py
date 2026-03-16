class NtripClient:
    def __init__(self) -> None:
        self.connected = False
        self.bytes_per_sec = 0

    def connect(self) -> None:
        self.connected = True
        self.bytes_per_sec = 128

    def disconnect(self) -> None:
        self.connected = False
        self.bytes_per_sec = 0
