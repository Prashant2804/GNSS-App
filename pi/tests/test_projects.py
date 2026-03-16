from fastapi.testclient import TestClient

from gnss_gateway.main import app


def test_project_crud() -> None:
    client = TestClient(app)
    create = client.post("/projects", json={"name": "Test"})
    assert create.status_code == 200
    project_id = create.json()["id"]

    get_resp = client.get(f"/projects/{project_id}")
    assert get_resp.status_code == 200

    update = client.put(f"/projects/{project_id}", json={"name": "Updated"})
    assert update.json()["name"] == "Updated"

    delete = client.delete(f"/projects/{project_id}")
    assert delete.json()["deleted"] is True
