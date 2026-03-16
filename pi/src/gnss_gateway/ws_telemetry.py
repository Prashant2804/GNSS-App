def build_mock_telemetry() -> dict:
    return {
        "fixQuality": "fix",
        "satellites": 8,
        "horizontalAccuracyM": 0.9,
        "verticalAccuracyM": 1.2,
        "ageOfDiffSec": 0.5,
        "updateRateHz": 1.0,
        "corrections": {"connected": False, "bytesPerSec": 0},
    }
