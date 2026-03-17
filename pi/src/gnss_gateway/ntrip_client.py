class NtripClient:
    def __init__(self) -> None:
        self.config: dict | None = None
        self.connected = False
        self.bytes_per_sec = 0

    def set_config(self, config: dict) -> None:
        self.config = config

    def connect(self) -> None:
        if self.config is None:
            raise RuntimeError("NTRIP config not set")
        self.connected = True
        self.bytes_per_sec = 128

    def disconnect(self) -> None:
        self.connected = False
        self.bytes_per_sec = 0
