---
schema_version: 1.0.0
document_type: plan
project_id: project_000038bq5wbtc2wp5tcqxr45tm
sprint_id: sprint_000038bq5z5bg201dyfmkcbdzc
phase: plan
status: draft
revision: 1
intent_ids: [intent_000038bq62x00g90ehjnwx1zd8, intent_000038bqgejkp1hm0r40dsdbv8]
generated_at: 2026-10-26T07:49:43Z
generated_by: tene-workflow
---

# plan — 运维平台商业化全面优化

<!-- tene:section:purpose -->
## Purpose

按用户明确授权实施上一轮74项优化，构建能演示且可真实操作的毛团子企业运维平台。

<!-- tene:section:scope -->
## Scope

根admin的18业务页、公共UI/权限/请求层、对应/ops后端和测试。保留现有Vue3/ElementPlus/SpringBoot/MyBatis栈；不部署、不提交、不改移动端、不改变数据库schema。

<!-- tene:section:layers -->
## Layers

Interface: Vue routes/forms/typed ops API. Business: auth, alarms, jobs, health/AI/business inspection. Persistence: existing ops/system_config/audit tables only. Runtime: Vite build, JDK17 Maven, local browser checks; external provider delivery needs configured credentials.

<!-- tene:section:six-questions -->
## Six questions

Ops API: admin/src/api/ops.ts referenced by all views; request/response follows actual controller DTO. Auth: request/store/router plus OpsAuthController/AdminAuthInterceptor; JWT+permission inputs, authenticated admin context output. UI: layout/styles/StatCard/chart helpers used by pages; explicit loading/error/empty/stale states. Existing backend fields are truth, unknown metrics never become zero.

<!-- tene:section:traceability -->
## Traceability

intent_000038bq62x00g90ehjnwx1zd8 / ac_000038bq62x004nz2ezxb62kjw. Checklist coverage: product1-6; business7-16; functions17-34; interaction35-44; UI45-54; UX55-64; security/engineering65-74. Evidence must distinguish fixtures, local runtime and real external integrations.

<!-- tene:section:decisions -->
## Decisions

User approved immediate complete implementation and one-time Tene adoption in this chat. Standard profile; no archive approval inferred. Preserve existing permissions and public login intent. No fake costs, provider availability or delivery receipts. Reuse current stack, add shared helpers instead of dependencies.

<!-- tene:section:freeform -->
## Freeform

ANEW methods supply changed/preserved criteria; cc4i reviews auth/API changes under the Tene lifecycle only. Repository has substantial pre-existing untracked files: baseline captured outside repo; no cleanup/reset/commit.

<!-- tene:section:work-packages -->
## Work packages

T1 root: auth/request/router/stores, shared test seams, integration and acceptance. T2 visual builder: layout/theme/login/dashboard/biz/StatCard and workbench. T3 ops pages builder: typed API and monitor/system/data/log/alarm/job/audit/config/instances pages. T4 backend builder: metric unknown/health/AI capabilities/alerts/config history notification behavior using existing persistence. T5 independent evaluator: requirements/diff/security/screenshots.

<!-- tene:section:dependencies -->
## Dependencies

T2/T3 consume the existing server contract; T4 extensions are additive only and coordinated with T3. Root owns canonical lifecycle and integration. Each builder runs GitNexus impact before existing symbols; HIGH/CRITICAL announced before edits.

<!-- tene:section:verification -->
## Verification

Targeted behavior tests for cancellation, auth, normalization, polling. npm run build. Maven targeted tests/compile with JDK17. Local browser with in-memory fixtures verifies UI only; actual backend verification separately. Desktop and narrow viewport screenshots. Use baseline hashes/diffs because source is untracked.

<!-- tene:section:risks -->
## Risks

Existing backend configs/toolchain may block full runtime; never replace with invented pass. Notification channels/costs require real configuration; UI must show unconfigured. Existing shared admin auth also affects apps/admin; preserve legacy response shape while adding ops flattening.

<!-- tene:section:yagni -->
## Yagni

No new front-end frameworks, global installs, databases, schema or external message sends.


## Implementation task ownership

task_000038bq6h8932scezm7n2gx64: authentication and integration. task_000038bqcma5e1kf6f1dg51j7r: UI and eighteen pages. task_000038bqcma95cas8y36be8pz4: backend reliability. task_000038bqcvd9nr2tg8nrsy6b7w: inventory pre-existing untracked baseline files only; these files were NOT authored by this sprint, and are not implementation coverage claims.

## Current authorized extension: 48 items
User explicitly requested implementation of the latest audit list on 2026-10-26. This extends the current sprint; prior failed QA remains historical evidence. intent_000038bqgejkp1hm0r40dsdbv8 / ac_000038bqgejkpp44carjntdxnm. See coverage-48.md for exact numbered criteria.

Changed: business queues and role-focused dashboard, safe isolated demo, onboarding, service targets and configurable thresholds, actual provider configuration, read/write permissions, durable incident notes, typed/exportable tables, dirty forms, Cron preview, fresh shell status and smaller chunks. Preserved: existing endpoints and old grants, successful auth behavior, cancellation safety, theme, schema, mobile code, default disabled notification transport.

Design: root adds route/dashboard/demo/request isolation; task_000038bqgj198221m0fgpjdv4g. Backend adds queries/adapters using existing persistence and compatible additive fields; task_000038bqgj10ynvjaz8k04pr3c. Operational views and permission actions task_000038bqgj13td02nbkhqzgwz4. Shared UX and visual task_000038bqgj16jwztszfp5khhx0. Six questions: calls enter router/ops functions, invoke permission-aware services, return existing envelope and additive DTOs; configuration lives in system_configs, data aggregation uses existing tables; real provider data is unknown unless configured.

Acceptance: all48 have concrete implementation paths and focused evidence; notification credentials and real upstream receipts/costs remain explicit external dependencies. Do not weaken failed environment acceptance. No production publishing/DB migration/external notification execution.
