import json
from pathlib import Path
from datetime import datetime
from typing import Optional

_active_session: Optional[str] = None
_base_dir = Path("/tmp/gnss-sessions")


def start_logging() -> str:
    global _active_session
    _base_dir.mkdir(parents=True, exist_ok=True)
    session_id = datetime.utcnow().strftime("%Y%m%dT%H%M%SZ")
    session_dir = _base_dir / session_id
    session_dir.mkdir(parents=True, exist_ok=True)
    manifest = {
        "sessionId": session_id,
        "startTime": datetime.utcnow().isoformat() + "Z",
        "receiver": {"model": "ZED-X20P"},
    }
    (session_dir / "session_manifest.json").write_text(json.dumps(manifest))
    _active_session = session_id
    return session_id


def stop_logging() -> Optional[str]:
    global _active_session
    session_id = _active_session
    _active_session = None
    return session_id
