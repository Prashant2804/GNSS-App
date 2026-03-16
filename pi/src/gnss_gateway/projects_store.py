import uuid

_projects: dict[str, dict] = {}


def list_projects() -> list:
    return list(_projects.values())


def create_project(name: str) -> dict:
    project_id = str(uuid.uuid4())
    project = {"id": project_id, "name": name}
    _projects[project_id] = project
    return project


def get_project(project_id: str) -> dict | None:
    return _projects.get(project_id)


def update_project(project_id: str, name: str) -> dict | None:
    project = _projects.get(project_id)
    if project is None:
        return None
    project["name"] = name
    return project


def delete_project(project_id: str) -> bool:
    return _projects.pop(project_id, None) is not None
