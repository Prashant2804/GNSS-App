from fastapi import FastAPI, WebSocket, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import asyncio

from .ws_telemetry import build_mock_telemetry
from .ntrip_client import NtripClient
from .logging_sessions import start_logging, stop_logging
from .projects_store import (
    list_projects,
    create_project,
    get_project,
    update_project,
    delete_project,
)

app = FastAPI()
_ntrip = NtripClient()


class ProjectCreate(BaseModel):
    name: str


class ProjectUpdate(BaseModel):
    name: str


@app.get("/health")
def health() -> JSONResponse:
    return JSONResponse({"status": "ok"})


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


@app.websocket("/ws/telemetry")
async def ws_telemetry(websocket: WebSocket) -> None:
    await websocket.accept()
    while True:
        telemetry = build_mock_telemetry()
        telemetry["corrections"]["connected"] = _ntrip.connected
        telemetry["corrections"]["bytesPerSec"] = _ntrip.bytes_per_sec
        await websocket.send_json(telemetry)
        await asyncio.sleep(1)
