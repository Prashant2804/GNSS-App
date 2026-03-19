"""Tests for raw observation storage and RINEX writer."""

from gnss_gateway.raw_observations import (
    build_mock_raw_epoch,
    start_recording,
    stop_recording,
    is_recording,
    get_epochs,
    add_epoch,
    clear_epochs,
)
from gnss_gateway.rinex_writer import generate_rinex


def test_mock_epoch_has_satellites():
    epoch = build_mock_raw_epoch()
    assert len(epoch.satellites) > 0
    assert epoch.gps_week > 0
    assert epoch.timestamp_utc.endswith("Z")


def test_recording_lifecycle():
    clear_epochs()
    assert not is_recording()
    start_recording()
    assert is_recording()
    epoch = build_mock_raw_epoch()
    add_epoch(epoch)
    assert len(get_epochs()) == 1
    stop_recording()
    assert not is_recording()
    add_epoch(build_mock_raw_epoch())  # should not add, not recording
    assert len(get_epochs()) == 1
    clear_epochs()
    assert len(get_epochs()) == 0


def test_generate_rinex_empty():
    result = generate_rinex([])
    assert result == ""


def test_generate_rinex_has_header_and_data():
    epochs = [build_mock_raw_epoch() for _ in range(3)]
    rinex = generate_rinex(epochs)
    assert "RINEX VERSION / TYPE" in rinex
    assert "END OF HEADER" in rinex
    assert "SYS / # / OBS TYPES" in rinex
    lines = rinex.split("\n")
    epoch_lines = [l for l in lines if l.startswith(">")]
    assert len(epoch_lines) == 3


def test_rinex_contains_gps_and_galileo():
    epochs = [build_mock_raw_epoch()]
    rinex = generate_rinex(epochs)
    assert "G" in rinex  # GPS system
    assert "E" in rinex  # Galileo
    assert "C1C" in rinex or "L1C" in rinex
