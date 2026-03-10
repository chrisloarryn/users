#!/usr/bin/env python3

from __future__ import annotations

import argparse
import re
from datetime import datetime, timezone
from pathlib import Path


START_MARKER = "<!-- validation-snapshot:start -->"
END_MARKER = "<!-- validation-snapshot:end -->"
UNIT_START_MARKER = "<!-- validation-unit:start -->"
UNIT_END_MARKER = "<!-- validation-unit:end -->"
KARATE_START_MARKER = "<!-- validation-karate:start -->"
KARATE_END_MARKER = "<!-- validation-karate:end -->"
FULL_START_MARKER = "<!-- validation-full:start -->"
FULL_END_MARKER = "<!-- validation-full:end -->"
COVERAGE_START_MARKER = "<!-- validation-coverage:start -->"
COVERAGE_END_MARKER = "<!-- validation-coverage:end -->"
GATLING_START_MARKER = "<!-- validation-gatling:start -->"
GATLING_END_MARKER = "<!-- validation-gatling:end -->"
CI_WORKFLOW_ANCHOR = "The GitHub Actions workflow lives in `.github/workflows/validate.yml`.\n"


def status_label(result: str) -> str:
    match result:
        case "success":
            return "PASS"
        case "failure":
            return "FAIL"
        case "skipped":
            return "SKIPPED"
        case "cancelled":
            return "CANCELLED"
        case _:
            return "UNKNOWN"


def display(value: str | None, default: str = "n/a") -> str:
    if value is None:
        return default
    value = value.strip()
    return value or default


def format_seconds(raw: str | None) -> str:
    try:
        return f"{float(raw):.2f}s"
    except (TypeError, ValueError):
        return "n/a"


def format_timestamp(raw: str) -> str:
    parsed = datetime.fromisoformat(raw.replace("Z", "+00:00"))
    return parsed.astimezone(timezone.utc).strftime("%B %d, %Y %H:%M UTC")


def parse_int(raw: str | None) -> int | None:
    try:
        return int(str(raw).strip())
    except (TypeError, ValueError):
        return None


def build_run_prefix(args: argparse.Namespace) -> str:
    run_url = f"https://github.com/{args.repository}/actions/runs/{args.run_id}"
    return f"Latest completed CI result from [`Validate Java Application #{args.run_number}`]({run_url}) on `{args.branch}`:"


def build_snapshot(args: argparse.Namespace) -> str:
    run_url = f"https://github.com/{args.repository}/actions/runs/{args.run_id}"
    commit_url = f"https://github.com/{args.repository}/commit/{args.commit_sha}"
    short_sha = args.commit_sha[:7]

    lines = [
        "### Latest CI Validation Snapshot",
        "_Automatically updated by `Validate Java Application` after push runs on `main` and `develop`._",
        "",
        f"- Run: [`#{args.run_number}`]({run_url})",
        f"- Branch: `{args.branch}`",
        f"- Commit: [`{short_sha}`]({commit_url})",
        f"- Updated: {format_timestamp(args.updated_at)}",
        "",
        "| Stage | Result | Highlights |",
        "| --- | --- | --- |",
        (
            f"| Unit and integration | {status_label(args.unit_result)} | "
            f"tests={display(args.unit_tests_run)}, failures={display(args.unit_failures)}, "
            f"errors={display(args.unit_errors)}, skipped={display(args.unit_skipped)}, "
            f"duration={format_seconds(args.unit_duration_seconds)}, svc={display(args.unit_service_tests)}, "
            f"sec={display(args.unit_security_tests)}, repo={display(args.unit_repository_tests)}, "
            f"api={display(args.unit_integration_tests)}, err={display(args.unit_error_tests)}, "
            f"other={display(args.unit_other_tests)} |"
        ),
        (
            f"| Karate contracts | {status_label(args.karate_result)} | "
            f"features={display(args.karate_features)}, scenarios={display(args.karate_scenarios)}, "
            f"failed={display(args.karate_failed)}, duration={format_seconds(args.karate_duration_seconds)} |"
        ),
        (
            f"| Gatling performance | {status_label(args.gatling_result)} | "
            f"requests={display(args.gatling_requests)}, ok={display(args.gatling_ok)}, "
            f"ko={display(args.gatling_ko)}, mean={display(args.gatling_mean_ms)}ms, "
            f"p95={display(args.gatling_p95)}ms, p99={display(args.gatling_p99)}ms, "
            f"throughput={display(args.gatling_throughput)}rps, "
            f"failed assertions={display(args.gatling_assertions_failed)} |"
        ),
        (
            f"| Coverage quality gate | {status_label(args.coverage_result)} | "
            f"line coverage={display(args.coverage_pct)}%, "
            f"threshold={display(args.coverage_threshold_pct)}%, "
            f"covered={display(args.coverage_covered_lines)}, "
            f"missed={display(args.coverage_missed_lines)} |"
        ),
        "",
        "Artifacts published by the run:",
        "- `unit-test-report`",
        "- `karate-report`",
        "- `gatling-report`",
        "- `coverage-report`",
    ]
    return "\n".join(lines)


def replace_block(text: str, start_marker: str, end_marker: str, content: str) -> str:
    replacement = f"{start_marker}\n{content}\n{end_marker}"
    if start_marker not in text or end_marker not in text:
        raise SystemExit(f"Could not locate README markers: {start_marker} / {end_marker}")

    return re.sub(
        rf"{re.escape(start_marker)}.*?{re.escape(end_marker)}",
        replacement,
        text,
        count=1,
        flags=re.DOTALL,
    )


def build_unit_block(args: argparse.Namespace) -> str:
    return "\n".join(
        [
            build_run_prefix(args),
            f"- result: `{status_label(args.unit_result)}`",
            f"- tests executed: `{display(args.unit_tests_run)}`",
            f"- failures: `{display(args.unit_failures)}`",
            f"- errors: `{display(args.unit_errors)}`",
            f"- skipped: `{display(args.unit_skipped)}`",
            f"- duration: `{format_seconds(args.unit_duration_seconds)}`",
        ]
    )


def build_karate_block(args: argparse.Namespace) -> str:
    return "\n".join(
        [
            build_run_prefix(args),
            f"- result: `{status_label(args.karate_result)}`",
            f"- features: `{display(args.karate_features)}`",
            f"- scenarios: `{display(args.karate_scenarios)}`",
            f"- failed scenarios: `{display(args.karate_failed)}`",
            f"- duration: `{format_seconds(args.karate_duration_seconds)}`",
        ]
    )


def build_full_block(args: argparse.Namespace) -> str:
    unit_tests = parse_int(args.unit_tests_run)
    karate_scenarios = parse_int(args.karate_scenarios)
    unit_failures = parse_int(args.unit_failures)
    karate_failed = parse_int(args.karate_failed)
    unit_errors = parse_int(args.unit_errors)

    total_checks = "n/a" if unit_tests is None or karate_scenarios is None else str(unit_tests + karate_scenarios)
    total_failures = "n/a" if unit_failures is None or karate_failed is None else str(unit_failures + karate_failed)
    total_errors = "n/a" if unit_errors is None else str(unit_errors)
    combined_result = "PASS" if args.unit_result == "success" and args.karate_result == "success" else "CHECK DETAILS"

    return "\n".join(
        [
            build_run_prefix(args),
            f"- combined result: `{combined_result}`",
            f"- total checks derived from unit + Karate: `{total_checks}`",
            f"- failures: `{total_failures}`",
            f"- errors: `{total_errors}`",
        ]
    )


def build_coverage_block(args: argparse.Namespace) -> str:
    return "\n".join(
        [
            build_run_prefix(args),
            f"- result: `{status_label(args.coverage_result)}`",
            f"- line coverage: `{display(args.coverage_pct)}%`",
            f"- covered lines: `{display(args.coverage_covered_lines)}`",
            f"- missed lines: `{display(args.coverage_missed_lines)}`",
            f"- minimum threshold: `{display(args.coverage_threshold_pct)}%`",
        ]
    )


def build_gatling_block(args: argparse.Namespace) -> str:
    return "\n".join(
        [
            build_run_prefix(args),
            f"- result: `{status_label(args.gatling_result)}`",
            f"- total requests: `{display(args.gatling_requests)}`",
            f"- successful requests: `{display(args.gatling_ok)}`",
            f"- failed requests: `{display(args.gatling_ko)}`",
            f"- mean response time: `{display(args.gatling_mean_ms)} ms`",
            f"- p95: `{display(args.gatling_p95)} ms`",
            f"- p99: `{display(args.gatling_p99)} ms`",
            f"- throughput: `{display(args.gatling_throughput)} rps`",
            f"- failed assertions: `{display(args.gatling_assertions_failed)}`",
        ]
    )


def replace_snapshot(readme_path: Path, snapshot: str, args: argparse.Namespace) -> None:
    text = readme_path.read_text(encoding="utf-8")

    text = replace_block(text, UNIT_START_MARKER, UNIT_END_MARKER, build_unit_block(args))
    text = replace_block(text, KARATE_START_MARKER, KARATE_END_MARKER, build_karate_block(args))
    text = replace_block(text, FULL_START_MARKER, FULL_END_MARKER, build_full_block(args))
    text = replace_block(text, COVERAGE_START_MARKER, COVERAGE_END_MARKER, build_coverage_block(args))
    text = replace_block(text, GATLING_START_MARKER, GATLING_END_MARKER, build_gatling_block(args))

    snapshot_replacement = f"{START_MARKER}\n{snapshot}\n{END_MARKER}"

    if START_MARKER in text and END_MARKER in text:
        text = re.sub(
            rf"{re.escape(START_MARKER)}.*?{re.escape(END_MARKER)}",
            snapshot_replacement,
            text,
            count=1,
            flags=re.DOTALL,
        )
    elif CI_WORKFLOW_ANCHOR in text:
        text = text.replace(
            CI_WORKFLOW_ANCHOR,
            CI_WORKFLOW_ANCHOR + "\n" + snapshot_replacement + "\n",
            1,
        )
    else:
        raise SystemExit("Could not locate CI workflow section in README.md")

    readme_path.write_text(text, encoding="utf-8")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--readme", required=True)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--run-number", required=True)
    parser.add_argument("--branch", required=True)
    parser.add_argument("--commit-sha", required=True)
    parser.add_argument("--updated-at", required=True)
    parser.add_argument("--unit-result", required=True)
    parser.add_argument("--unit-tests-run", required=True)
    parser.add_argument("--unit-failures", required=True)
    parser.add_argument("--unit-errors", required=True)
    parser.add_argument("--unit-skipped", required=True)
    parser.add_argument("--unit-duration-seconds", required=True)
    parser.add_argument("--unit-service-tests", required=True)
    parser.add_argument("--unit-security-tests", required=True)
    parser.add_argument("--unit-repository-tests", required=True)
    parser.add_argument("--unit-integration-tests", required=True)
    parser.add_argument("--unit-error-tests", required=True)
    parser.add_argument("--unit-other-tests", required=True)
    parser.add_argument("--karate-result", required=True)
    parser.add_argument("--karate-features", required=True)
    parser.add_argument("--karate-scenarios", required=True)
    parser.add_argument("--karate-failed", required=True)
    parser.add_argument("--karate-duration-seconds", required=True)
    parser.add_argument("--gatling-result", required=True)
    parser.add_argument("--gatling-requests", required=True)
    parser.add_argument("--gatling-ok", required=True)
    parser.add_argument("--gatling-ko", required=True)
    parser.add_argument("--gatling-mean-ms", required=True)
    parser.add_argument("--gatling-p95", required=True)
    parser.add_argument("--gatling-p99", required=True)
    parser.add_argument("--gatling-throughput", required=True)
    parser.add_argument("--gatling-assertions-failed", required=True)
    parser.add_argument("--coverage-result", required=True)
    parser.add_argument("--coverage-pct", required=True)
    parser.add_argument("--coverage-threshold-pct", required=True)
    parser.add_argument("--coverage-covered-lines", required=True)
    parser.add_argument("--coverage-missed-lines", required=True)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    snapshot = build_snapshot(args)
    replace_snapshot(Path(args.readme), snapshot, args)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
