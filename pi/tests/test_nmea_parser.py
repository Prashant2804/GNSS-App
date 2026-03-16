from gnss_gateway.nmea_parser import parse_gga


def test_parse_gga() -> None:
    sentence = "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47"
    parsed = parse_gga(sentence)
    assert parsed["fixQuality"] == "fix"
    assert parsed["satellites"] == 8
