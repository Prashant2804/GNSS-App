from dataclasses import dataclass
from pathlib import Path
import yaml


@dataclass
class GatewayConfig:
    serial_port: str = "/dev/ttyS0"
    baud_rate: int = 115200


def load_config(path: str) -> GatewayConfig:
    config_path = Path(path)
    if not config_path.exists():
        return GatewayConfig()
    data = yaml.safe_load(config_path.read_text()) or {}
    return GatewayConfig(
        serial_port=data.get("serial_port", "/dev/ttyS0"),
        baud_rate=int(data.get("baud_rate", 115200)),
    )
