import math


_t = 0


def build_mock_telemetry() -> dict:
    global _t
    _t += 1
    # Deterministic circular path around a fixed point (for easy validation).
    center_lat = 12.9716
    center_lon = 77.5946
    # ~5m radius in degrees (roughly; good enough for mock UI).
    radius_deg = 0.00005
    angle = (_t % 360) * math.pi / 180.0
    lat = center_lat + radius_deg * math.cos(angle)
    lon = center_lon + radius_deg * math.sin(angle)
    return {
        "fixQuality": "fix",
        "satellites": 8,
        "latitudeDeg": lat,
        "longitudeDeg": lon,
        "altitudeMSL": 900.0,
        "horizontalAccuracyM": 0.9,
        "verticalAccuracyM": 1.2,
        "ageOfDiffSec": 0.5,
        "updateRateHz": 1.0,
        "corrections": {"connected": False, "bytesPerSec": 0},
    }
