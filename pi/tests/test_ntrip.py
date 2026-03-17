from fastapi.testclient import TestClient

from gnss_gateway.main import app


def test_ntrip_requires_config_before_connect() -> None:
    client = TestClient(app)
    resp = client.post("/ntrip/connect")
    assert resp.status_code == 400


def test_ntrip_config_roundtrip_and_connect_disconnect() -> None:
    client = TestClient(app)

    cfg = {
        "casterHost": "caster.example.com",
        "casterPort": 2101,
        "mountPoint": "MOUNT",
        "username": "u",
        "password": "p",
    }

    stored = client.post("/ntrip/config", json=cfg)
    assert stored.status_code == 200
    assert stored.json()["stored"] is True

    get_cfg = client.get("/ntrip/config")
    assert get_cfg.status_code == 200
    assert get_cfg.json()["config"]["casterHost"] == "caster.example.com"

    conn = client.post("/ntrip/connect")
    assert conn.status_code == 200
    assert conn.json()["connected"] is True

    disc = client.post("/ntrip/disconnect")
    assert disc.status_code == 200
    assert disc.json()["connected"] is False

