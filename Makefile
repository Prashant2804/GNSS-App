.PHONY: bootstrap lint test run-pi-local run-android e2e deploy-pi

bootstrap:
	@echo "Bootstrap: ensure Java/Android SDK and Python 3.11+ are installed."
	@echo "Hint: Android Gradle wrapper will download on first build."

lint:
	@./scripts/ci/lint_all.sh

test:
	@./scripts/ci/check_openapi.sh
	@cd pi && pytest
	@cd android && ./gradlew testDebugUnitTest

run-pi-local:
	@cd pi && uvicorn gnss_gateway.main:app --reload --port 8000

run-android:
	@cd android && ./gradlew installDebug

e2e:
	@./scripts/dev/e2e_smoke.sh

deploy-pi:
	@./scripts/deploy/update_pi_service.sh
