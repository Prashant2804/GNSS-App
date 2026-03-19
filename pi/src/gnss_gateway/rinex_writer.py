"""
RINEX 3.04 observation file writer.

Generates valid RINEX 3.04 OBS files from stored ObservationEpoch data.
Reference: ftp://igs.org/pub/data/format/rinex304.pdf
"""

from datetime import datetime, timezone
from typing import List

from .raw_observations import ObservationEpoch, SatObservation

RINEX_VERSION = "3.04"

# Map our signal codes to RINEX 3 observation codes
_SIGNAL_TO_RINEX = {
    ("GPS", "L1C"): {"C": "C1C", "L": "L1C", "D": "D1C", "S": "S1C"},
    ("GPS", "L2W"): {"C": "C2W", "L": "L2W", "D": "D2W", "S": "S2W"},
    ("GAL", "E1C"): {"C": "C1C", "L": "L1C", "D": "D1C", "S": "S1C"},
}

_GNSS_LETTER = {"GPS": "G", "GLO": "R", "GAL": "E", "BDS": "C"}


def _fmt_rinex_float(val, width=14, decimals=3):
    """Format a float in RINEX fixed-width style."""
    if val is None:
        return " " * width
    fmt = f"{val:{width}.{decimals}f}"
    return fmt[:width]


def _collect_obs_types(epochs: List[ObservationEpoch]) -> dict:
    """Determine observation types present per GNSS system."""
    sys_obs = {}
    for ep in epochs:
        for sat in ep.satellites:
            letter = _GNSS_LETTER.get(sat.gnss_id, "G")
            key = (sat.gnss_id, sat.signal)
            codes = _SIGNAL_TO_RINEX.get(key)
            if codes is None:
                continue
            if letter not in sys_obs:
                sys_obs[letter] = set()
            for rinex_code in codes.values():
                sys_obs[letter].add(rinex_code)
    # Sort for determinism
    return {k: sorted(v) for k, v in sorted(sys_obs.items())}


def write_rinex_header(
    epochs: List[ObservationEpoch],
    marker_name: str = "GNSSFLOW",
    receiver_type: str = "ZED-X20P",
    antenna_type: str = "INTERNAL",
    approx_lat: float = 0.0,
    approx_lon: float = 0.0,
    approx_alt: float = 0.0,
) -> str:
    lines = []

    lines.append(f"     {RINEX_VERSION}           OBSERVATION DATA    M                   RINEX VERSION / TYPE")
    now = datetime.now(timezone.utc).strftime("%Y%m%d %H%M%S UTC")
    lines.append(f"GNSS-Flow           gnss-flow           {now} PGM / RUN BY / DATE")
    lines.append(f"{marker_name:<60s}MARKER NAME")
    lines.append(f"{'0001':<20s}{'GNSS-Flow':<40s}MARKER NUMBER")
    lines.append(f"{'operator':<20s}{'GNSS-Flow':<40s}OBSERVER / AGENCY")
    lines.append(f"{'001':<20s}{receiver_type:<20s}{'1.0':<20s}REC # / TYPE / VERS")
    lines.append(f"{'001':<20s}{antenna_type:<20s}{'':<20s}ANT # / TYPE")

    # Approx position in ECEF (simplified — for header only)
    import math
    lat_r = math.radians(approx_lat)
    lon_r = math.radians(approx_lon)
    a = 6378137.0
    f = 1 / 298.257223563
    e2 = 2 * f - f * f
    n_val = a / math.sqrt(1 - e2 * math.sin(lat_r) ** 2)
    x = (n_val + approx_alt) * math.cos(lat_r) * math.cos(lon_r)
    y = (n_val + approx_alt) * math.cos(lat_r) * math.sin(lon_r)
    z = (n_val * (1 - e2) + approx_alt) * math.sin(lat_r)
    lines.append(f"{x:14.4f}{y:14.4f}{z:14.4f}                  APPROX POSITION XYZ")
    lines.append(f"{0.0:14.4f}{0.0:14.4f}{0.0:14.4f}                  ANTENNA: DELTA H/E/N")

    sys_obs = _collect_obs_types(epochs)
    for sys_letter, obs_list in sys_obs.items():
        n = len(obs_list)
        obs_str = "".join(f" {o:>3s}" for o in obs_list[:13])
        lines.append(f"{sys_letter}   {n:3d}{obs_str:<52s}SYS / # / OBS TYPES")
        remaining = obs_list[13:]
        while remaining:
            chunk = remaining[:13]
            remaining = remaining[13:]
            obs_str = "".join(f" {o:>3s}" for o in chunk)
            lines.append(f"      {obs_str:<54s}SYS / # / OBS TYPES")

    if epochs:
        first = epochs[0]
        dt = datetime.fromisoformat(first.timestamp_utc.replace("Z", "+00:00"))
        lines.append(f"  {dt.year:4d}    {dt.month:2d}    {dt.day:2d}    {dt.hour:2d}    {dt.minute:2d}   {dt.second:2d}.{dt.microsecond // 1000:03d}0000     GPS         TIME OF FIRST OBS")

    lines.append(f"{'':60s}END OF HEADER")
    return "\n".join(lines) + "\n"


def write_rinex_epoch(epoch: ObservationEpoch, sys_obs: dict) -> str:
    """Write a single observation epoch record."""
    dt = datetime.fromisoformat(epoch.timestamp_utc.replace("Z", "+00:00"))
    epoch_flag = 0
    # Count unique satellites
    sat_set = set()
    for s in epoch.satellites:
        letter = _GNSS_LETTER.get(s.gnss_id, "G")
        sat_set.add(f"{letter}{s.sv_id:02d}")
    num_sats = len(sat_set)

    header_line = f"> {dt.year:4d} {dt.month:02d} {dt.day:02d} {dt.hour:02d} {dt.minute:02d}{dt.second:11.7f}  {epoch_flag}{num_sats:3d}      {epoch.receiver_clock_bias_s:15.12f}"

    lines = [header_line]

    # Group satellites
    sat_data = {}
    for s in epoch.satellites:
        letter = _GNSS_LETTER.get(s.gnss_id, "G")
        sat_key = f"{letter}{s.sv_id:02d}"
        if sat_key not in sat_data:
            sat_data[sat_key] = {}
        key = (s.gnss_id, s.signal)
        codes = _SIGNAL_TO_RINEX.get(key, {})
        if s.pseudorange_m is not None:
            sat_data[sat_key][codes.get("C", "")] = s.pseudorange_m
        if s.carrier_phase_cycles is not None:
            sat_data[sat_key][codes.get("L", "")] = s.carrier_phase_cycles
        if s.doppler_hz is not None:
            sat_data[sat_key][codes.get("D", "")] = s.doppler_hz
        if s.cno_dbhz:
            sat_data[sat_key][codes.get("S", "")] = s.cno_dbhz

    for sat_key in sorted(sat_data.keys()):
        sys_letter = sat_key[0]
        obs_types = sys_obs.get(sys_letter, [])
        vals = sat_data[sat_key]
        row = sat_key
        for obs_code in obs_types:
            v = vals.get(obs_code)
            row += _fmt_rinex_float(v, 14, 3)
            row += " "  # LLI + signal strength placeholder
        lines.append(row)

    return "\n".join(lines) + "\n"


def generate_rinex(
    epochs: List[ObservationEpoch],
    marker_name: str = "GNSSFLOW",
    approx_lat: float = 12.9716,
    approx_lon: float = 77.5946,
    approx_alt: float = 900.0,
) -> str:
    """Generate a complete RINEX 3.04 observation file string."""
    if not epochs:
        return ""

    header = write_rinex_header(
        epochs,
        marker_name=marker_name,
        approx_lat=approx_lat,
        approx_lon=approx_lon,
        approx_alt=approx_alt,
    )
    sys_obs = _collect_obs_types(epochs)

    body_parts = []
    for ep in epochs:
        body_parts.append(write_rinex_epoch(ep, sys_obs))

    return header + "".join(body_parts)
