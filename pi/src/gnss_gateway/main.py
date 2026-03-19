from fastapi import FastAPI, WebSocket, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import asyncio

from .ws_telemetry import build_mock_telemetry
from .ntrip_client import NtripClient
from .logging_sessions import start_logging, stop_logging
from .raw_observations import (
    start_recording,
    stop_recording,
    is_recording,
    get_epochs,
    clear_epochs,
    add_epoch,
    build_mock_raw_epoch,
    epoch_to_dict,
)
from .rinex_writer import generate_rinex
from .projects_store import (
    list_projects,
    create_project,
    get_project,
    update_project,
    delete_project,
)

app = FastAPI()
_ntrip = NtripClient()
_imu_enabled = False


class ProjectCreate(BaseModel):
    name: str


class ProjectUpdate(BaseModel):
    name: str


class NtripConfig(BaseModel):
    casterHost: str
    casterPort: int = 2101
    mountPoint: str
    username: str | None = None
    password: str | None = None


class ImuEnabled(BaseModel):
    enabled: bool


@app.get("/health")
def health() -> JSONResponse:
    return JSONResponse({"status": "ok"})


@app.get("/ntrip/config")
def ntrip_get_config() -> JSONResponse:
    return JSONResponse({"config": _ntrip.config})


@app.post("/ntrip/config")
def ntrip_set_config(payload: NtripConfig) -> JSONResponse:
    _ntrip.set_config(payload.model_dump())
    return JSONResponse({"stored": True})


@app.post("/ntrip/connect")
def ntrip_connect() -> JSONResponse:
    try:
        _ntrip.connect()
        return JSONResponse({"connected": True})
    except RuntimeError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/ntrip/disconnect")
def ntrip_disconnect() -> JSONResponse:
    _ntrip.disconnect()
    return JSONResponse({"connected": False})


@app.get("/imu/enabled")
def imu_get_enabled() -> JSONResponse:
    return JSONResponse({"enabled": _imu_enabled})


@app.post("/imu/enabled")
def imu_set_enabled(payload: ImuEnabled) -> JSONResponse:
    global _imu_enabled
    _imu_enabled = payload.enabled
    return JSONResponse({"enabled": _imu_enabled})


@app.get("/device/info")
def device_info() -> JSONResponse:
    return JSONResponse({"model": "ZED-X20P", "paired": False})


@app.post("/device/config")
def device_config() -> JSONResponse:
    return JSONResponse({"status": "stored"})


@app.post("/logs/start")
def logs_start() -> JSONResponse:
    session_id = start_logging()
    return JSONResponse({"sessionId": session_id})


@app.post("/logs/stop")
def logs_stop() -> JSONResponse:
    session_id = stop_logging()
    return JSONResponse({"sessionId": session_id})


@app.get("/projects")
def projects_list() -> JSONResponse:
    return JSONResponse({"items": list_projects()})


@app.post("/projects")
def projects_create(payload: ProjectCreate) -> JSONResponse:
    return JSONResponse(create_project(payload.name))


@app.get("/projects/{project_id}")
def projects_get(project_id: str) -> JSONResponse:
    project = get_project(project_id)
    if project is None:
        raise HTTPException(status_code=404, detail="Not found")
    return JSONResponse(project)


@app.put("/projects/{project_id}")
def projects_update(project_id: str, payload: ProjectUpdate) -> JSONResponse:
    project = update_project(project_id, payload.name)
    if project is None:
        raise HTTPException(status_code=404, detail="Not found")
    return JSONResponse(project)


@app.delete("/projects/{project_id}")
def projects_delete(project_id: str) -> JSONResponse:
    if not delete_project(project_id):
        raise HTTPException(status_code=404, detail="Not found")
    return JSONResponse({"deleted": True})


@app.post("/observations/start")
def obs_start() -> JSONResponse:
    start_recording()
    return JSONResponse({"recording": True})


@app.post("/observations/stop")
def obs_stop() -> JSONResponse:
    stop_recording()
    return JSONResponse({"recording": False, "epochCount": len(get_epochs())})


@app.get("/observations/status")
def obs_status() -> JSONResponse:
    return JSONResponse({"recording": is_recording(), "epochCount": len(get_epochs())})


@app.get("/observations/rinex")
def obs_rinex(marker: str = "GNSSFLOW") -> JSONResponse:
    epochs = get_epochs()
    if not epochs:
        raise HTTPException(status_code=404, detail="No observation epochs recorded")
    rinex = generate_rinex(epochs, marker_name=marker)
    from fastapi.responses import PlainTextResponse
    return PlainTextResponse(content=rinex, media_type="text/plain")


@app.delete("/observations")
def obs_clear() -> JSONResponse:
    clear_epochs()
    return JSONResponse({"cleared": True})


@app.websocket("/ws/telemetry")
async def ws_telemetry(websocket: WebSocket) -> None:
    await websocket.accept()
    while True:
        telemetry = build_mock_telemetry(imu_enabled=_imu_enabled)
        telemetry["corrections"]["connected"] = _ntrip.connected
        telemetry["corrections"]["bytesPerSec"] = _ntrip.bytes_per_sec

        raw_epoch = build_mock_raw_epoch()
        add_epoch(raw_epoch)
        telemetry["rawObservation"] = epoch_to_dict(raw_epoch)

        await websocket.send_json(telemetry)
        await asyncio.sleep(1)
