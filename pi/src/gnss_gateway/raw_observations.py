"""
Raw GNSS observation storage and mock generation.

In production the u-blox ZED-X20P sends UBX-RXM-RAWX messages which
contain per-satellite pseudorange, carrier phase, doppler, and CNO.
This module stores observation epochs and provides mock data for dev.
"""

import math
import time
from dataclasses import dataclass, field, asdict
from typing import List, Optional

_t_offset = 0


@dataclass
class SatObservation:
    """Single satellite observation within an epoch."""
    gnss_id: str  # "GPS", "GLO", "GAL", "BDS"
    sv_id: int  # satellite vehicle number
    signal: str  # e.g. "L1C", "L2W", "E1C"
    pseudorange_m: Optional[float] = None
    carrier_phase_cycles: Optional[float] = None
    doppler_hz: Optional[float] = None
    cno_dbhz: float = 0.0


@dataclass
class ObservationEpoch:
    """One epoch of raw GNSS observations."""
    timestamp_utc: str  # ISO 8601
    gps_week: int = 0
    gps_tow_s: float = 0.0
    receiver_clock_bias_s: float = 0.0
    satellites: List[SatObservation] = field(default_factory=list)


_epochs: List[ObservationEpoch] = []
_recording = False


def start_recording():
    global _recording, _epochs
    _recording = True
    _epochs.clear()


def stop_recording():
    global _recording
    _recording = False


def is_recording() -> bool:
    return _recording


def get_epochs() -> List[ObservationEpoch]:
    return list(_epochs)


def clear_epochs():
    _epochs.clear()


def add_epoch(epoch: ObservationEpoch):
    if _recording:
        _epochs.append(epoch)


def epoch_to_dict(epoch: ObservationEpoch) -> dict:
    return asdict(epoch)


# GPS epoch: Jan 6 1980 00:00:00 UTC
_GPS_EPOCH_UNIX = 315964800
_SECS_PER_WEEK = 604800


def _unix_to_gps(unix_ts: float):
    gps_secs = unix_ts - _GPS_EPOCH_UNIX + 18  # +18 leap seconds as of 2024
    week = int(gps_secs // _SECS_PER_WEEK)
    tow = gps_secs % _SECS_PER_WEEK
    return week, tow


def build_mock_raw_epoch() -> ObservationEpoch:
    """Generate a realistic-looking observation epoch for dev/testing."""
    global _t_offset
    _t_offset += 1

    now = time.time()
    week, tow = _unix_to_gps(now)

    from datetime import datetime, timezone
    ts = datetime.fromtimestamp(now, tz=timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"

    sats = []
    # GPS L1C/A + L2W for PRN 1-8
    for prn in range(1, 9):
        base_pr = 20_000_000.0 + prn * 1_000_000.0 + 50.0 * math.sin(_t_offset * 0.01 + prn)
        l1_freq = 1575.42e6
        l1_wavelength = 299792458.0 / l1_freq
        carrier = base_pr / l1_wavelength + 0.1 * math.sin(_t_offset * 0.02 + prn)
        doppler = -500.0 + 100.0 * math.sin(_t_offset * 0.05 + prn)
        cno = 42.0 + 5.0 * math.sin(_t_offset * 0.03 + prn)

        sats.append(SatObservation(
            gnss_id="GPS", sv_id=prn, signal="L1C",
            pseudorange_m=base_pr,
            carrier_phase_cycles=carrier,
            doppler_hz=doppler,
            cno_dbhz=cno,
        ))

        l2_freq = 1227.60e6
        l2_wavelength = 299792458.0 / l2_freq
        sats.append(SatObservation(
            gnss_id="GPS", sv_id=prn, signal="L2W",
            pseudorange_m=base_pr * (l2_freq / l1_freq),
            carrier_phase_cycles=base_pr / l2_wavelength + 0.08 * math.sin(_t_offset * 0.02 + prn),
            doppler_hz=doppler * (l2_freq / l1_freq),
            cno_dbhz=cno - 3.0,
        ))

    # Galileo E1C for E01-E04
    for prn in range(1, 5):
        base_pr = 22_000_000.0 + prn * 800_000.0
        freq = 1575.42e6
        wl = 299792458.0 / freq
        sats.append(SatObservation(
            gnss_id="GAL", sv_id=prn, signal="E1C",
            pseudorange_m=base_pr,
            carrier_phase_cycles=base_pr / wl,
            doppler_hz=-300.0 + 80.0 * math.sin(_t_offset * 0.04 + prn),
            cno_dbhz=40.0 + 4.0 * math.sin(_t_offset * 0.03 + prn),
        ))

    return ObservationEpoch(
        timestamp_utc=ts,
        gps_week=week,
        gps_tow_s=round(tow, 3),
        receiver_clock_bias_s=1.5e-8,
        satellites=sats,
    )
