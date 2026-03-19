.PHONY: bootstrap lint test run-pi-local run-android e2e deploy-pi

# Detect OS for cross-platform gradlew
ifeq ($(OS),Windows_NT)
  GRADLEW = gradlew.bat
  PYTHON  = python
  ACTIVATE = .venv\Scripts\activate &&
else
  GRADLEW = ./gradlew
  PYTHON  = python3
  ACTIVATE = . .venv/bin/activate &&
endif

bootstrap:
	@echo "=== Bootstrap ==="
	@echo "Ensure Java 21+, Android SDK, and Python 3.11+ are installed."
	@echo "Pi dependencies:"
	cd pi && $(PYTHON) -m venv .venv && $(ACTIVATE) pip install -e ".[dev]"
	@echo "Android: Gradle wrapper will download on first build."
	@echo "=== Done ==="

lint:
	@echo "=== Linting Pi ==="
	cd pi && $(ACTIVATE) $(PYTHON) -m ruff check . || true
	@echo "=== Linting Android ==="
	cd android && $(GRADLEW) lint

test:
	@echo "=== Pi tests ==="
	cd pi && $(ACTIVATE) pytest --tb=short -q
	@echo "=== Android tests ==="
	cd android && $(GRADLEW) testDebugUnitTest --no-daemon

run-pi-local:
	cd pi && $(ACTIVATE) PYTHONPATH=src $(PYTHON) -m uvicorn gnss_gateway.main:app --reload --port 8000

run-android:
	cd android && $(GRADLEW) installDebug

e2e:
	@./scripts/dev/e2e_smoke.sh

deploy-pi:
	@HOST=$(HOST) ./scripts/deploy/update_pi_service.sh
