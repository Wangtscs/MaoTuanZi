---
schema_version: 1.0.0
document_type: prd
project_id: project_000038bq5wbtc2wp5tcqxr45tm
sprint_id: sprint_000038bq5z5bg201dyfmkcbdzc
phase: prd
status: draft
revision: 1
intent_ids: [intent_000038bq62x00g90ehjnwx1zd8, intent_000038bqgejkp1hm0r40dsdbv8]
generated_at: 2026-10-26T07:49:43Z
generated_by: tene-workflow
---

# prd — 运维平台商业化全面优化

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

<!-- tene:section:problem -->
## Problem

Cancellation continues destructive operations; auth and many ops contracts mismatch; charts race DOM; unknown collection masks incidents; information hierarchy/themes/accessibility incomplete.

<!-- tene:section:actors -->
## Actors

Developers: locate trace/task failures. On-call ops: acknowledge/resolve incidents safely. Administrators: configure jobs/rules/permissions. Read-only visitors: inspect permitted data without mutations.

<!-- tene:section:journeys -->
## Journeys

Login -> role-aware overview -> incident/task -> filtered logs/history -> safe action -> actual result/audit. AI workload and provider capability -> dependency diagnosis. Environment/version/last update always visible.

<!-- tene:section:acceptance-criteria -->
## Acceptance criteria

A Auth contracts/header/permissions work and forbidden direct URLs/actions are denied. B All existing ops operations use real method/fields/pagination and cancel sends zero mutation. C Overview/AI inspection use backend facts, unknown state explicit. D 18 pages usable with responsive dual themes and consistent status states. E Alarms retain unknown samples, notification delivery states truthful; config changes traceable. F Builds/tests pass; browser fixture screenshots and interaction checks are labeled; external dependencies are reported without fake acceptance.

<!-- tene:section:non-goals -->
## Non goals

No production data modification/deployment, no real payment, no invented customer data, no speculative new infra or database migration.


## Current authorized extension: 48 items
User explicitly requested implementation of the latest audit list on 2026-10-26. This extends the current sprint; prior failed QA remains historical evidence. intent_000038bqgejkp1hm0r40dsdbv8 / ac_000038bqgejkpp44carjntdxnm. See coverage-48.md for exact numbered criteria.

Changed: business queues and role-focused dashboard, safe isolated demo, onboarding, service targets and configurable thresholds, actual provider configuration, read/write permissions, durable incident notes, typed/exportable tables, dirty forms, Cron preview, fresh shell status and smaller chunks. Preserved: existing endpoints and old grants, successful auth behavior, cancellation safety, theme, schema, mobile code, default disabled notification transport.

Design: root adds route/dashboard/demo/request isolation; task_000038bqgj198221m0fgpjdv4g. Backend adds queries/adapters using existing persistence and compatible additive fields; task_000038bqgj10ynvjaz8k04pr3c. Operational views and permission actions task_000038bqgj13td02nbkhqzgwz4. Shared UX and visual task_000038bqgj16jwztszfp5khhx0. Six questions: calls enter router/ops functions, invoke permission-aware services, return existing envelope and additive DTOs; configuration lives in system_configs, data aggregation uses existing tables; real provider data is unknown unless configured.

Acceptance: all48 have concrete implementation paths and focused evidence; notification credentials and real upstream receipts/costs remain explicit external dependencies. Do not weaken failed environment acceptance. No production publishing/DB migration/external notification execution.
