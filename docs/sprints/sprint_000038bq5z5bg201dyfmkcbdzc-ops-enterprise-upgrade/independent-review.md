# Independent review

Reviewed on 2026-10-26 under the existing Tene sprint, using cc4i adversarial review and secure API review methods. This review created no additional lifecycle and performed no real business mutation or external notification.

## Verdict

No confirmed open P0/P1 issue was found in the reviewed final changes. The previously reported P1 alarm-only permission defect is closed by code review and the targeted frontend regression test.

## Closed finding

An account with only `ops.alarm` previously entered the rule page but failed its combined load because the metric catalog required `ops.metrics`. `admin/src/views/alarm/rules.vue` now calls `alarmMetricCatalog()`, mapped to `/ops/alarm/metric-catalog`. `OpsAlarmController.metricCatalog()` requires `ops.alarm` and returns catalog metadata only. The monitor controller retains `ops.metrics`; monitoring access was not broadened. The backend permission/catalog test checks the endpoint annotations and verifies that the metadata path never collects runtime metrics.

## Authentication and boundary checks

- Reviewed `authPolicy.ts`, `request.ts`, `stores/user.ts`, `router/index.ts`, login and recovery/logout callers: safe internal return paths, permission wildcard boundaries, route denial, recoverable profile outages, normalized single Bearer header, 403 session preservation, single redirect for parallel 401s, and profile generation handling are coherent.
- Reviewed `JwtUtil`, `AdminPermissions`, `AdminAuthInterceptor`, `OpsAuthController`, `AdminServiceImpl.logout`, and `OpsAuthenticationContractTest`: admin signing/parsing uses the configured key; JSON permission collections normalize correctly; ops login preserves the legacy nested envelope; `/me` reads current database identity/permissions; Redis revocation failures propagate instead of reporting confirmed remote logout. The frontend distinguishes local logout from an unconfirmed server logout.
- Earlier review also checked confirmation cancellation, mutation single-flight locks, unknown/nonfinite alarm samples, configuration masking/history/rollback, and notification receipt wording. No additional confirmed P0/P1 emerged in those reviewed paths.

## Evidence and limits

- Independently reran `node --test admin/tests/ops-contracts.test.mjs admin/tests/auth-contracts.test.mjs` after the axios test-mock correction: **25 passed, 0 failed**. An earlier run encountered the known axios default-export mock error; it was a test harness issue and the final rerun passed.
- Read the existing Maven Surefire reports: `OpsEnterpriseReliabilityTest` 10/10, `OpsAuthenticationContractTest` 7/7, and `OpsAlarmCatalogPermissionTest` 2/2, all with zero failures/errors. These **19 backend tests were not independently rerun by this reviewer**.
- This is focused source/contract and local regression review, not live SQL/Redis authentication acceptance, production runtime acceptance, or proof of external notification/personnel delivery. Existing JWT permission snapshots remain the backend authorization model; this review does not assert immediate permission revocation for already issued tokens.
