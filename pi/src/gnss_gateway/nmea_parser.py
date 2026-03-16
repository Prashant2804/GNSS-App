def _parse_lat_lon(value: str, hemi: str) -> float:
    if not value:
        return 0.0
    deg = float(value[:2])
    minutes = float(value[2:])
    coord = deg + minutes / 60.0
    if hemi in ("S", "W"):
        coord = -coord
    return coord


def parse_gga(sentence: str) -> dict:
    parts = sentence.split(",")
    if len(parts) < 10:
        return {"fixQuality": "none", "satellites": 0}
    lat = _parse_lat_lon(parts[2], parts[3])
    lon = _parse_lat_lon(parts[4], parts[5])
    fix = parts[6] or "0"
    sats = int(parts[7] or "0")
    hdop = float(parts[8] or "99.0")
    alt = float(parts[9] or "0.0")
    fix_quality = "fix" if fix != "0" else "none"
    return {
        "fixQuality": fix_quality,
        "satellites": sats,
        "horizontalAccuracyM": hdop,
        "latitude": lat,
        "longitude": lon,
        "altitudeM": alt,
    }
