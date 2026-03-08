#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def append_summary(summary_file: str | None, content: str) -> None:
    if not summary_file:
        return
    with Path(summary_file).open("a", encoding="utf-8") as handle:
        handle.write(content.rstrip() + "\n")


def write_output(output_file: str | None, key: str, value: str) -> None:
    if not output_file:
        return
    with Path(output_file).open("a", encoding="utf-8") as handle:
        handle.write(f"{key}={value}\n")


def format_duration(seconds: float) -> str:
    return f"{seconds:.2f}s"


def categorize_suite_name(suite_name: str) -> str:
    if ".service." in suite_name:
        return "service"
    if ".security." in suite_name:
        return "security"
    if ".repository." in suite_name:
        return "repository"
    if ".integration." in suite_name:
        return "integration"
    if ".error." in suite_name:
        return "error"
    return "other"


def parse_unit_reports(args: argparse.Namespace) -> int:
    reports_dir = Path(args.reports_dir)
    totals = {
        "tests_run": 0,
        "failures": 0,
        "errors": 0,
        "skipped": 0,
        "duration_seconds": 0.0,
    }
    suites: list[tuple[str, int]] = []
    categories = {
        "service": {"suites": 0, "tests": 0},
        "security": {"suites": 0, "tests": 0},
        "repository": {"suites": 0, "tests": 0},
        "integration": {"suites": 0, "tests": 0},
        "error": {"suites": 0, "tests": 0},
        "other": {"suites": 0, "tests": 0},
    }

    for report in sorted(reports_dir.glob("TEST-*.xml")):
        root = ET.parse(report).getroot()
        suite_name = root.attrib.get("name", report.stem.removeprefix("TEST-"))
        if suite_name == "karate.ApiContractsKarateTest":
            continue

        tests = int(root.attrib.get("tests", "0"))
        totals["tests_run"] += tests
        totals["failures"] += int(root.attrib.get("failures", "0"))
        totals["errors"] += int(root.attrib.get("errors", "0"))
        totals["skipped"] += int(root.attrib.get("skipped", "0"))
        totals["duration_seconds"] += float(root.attrib.get("time", "0"))
        suites.append((suite_name, tests))
        category = categorize_suite_name(suite_name)
        categories[category]["suites"] += 1
        categories[category]["tests"] += tests

    for key, value in totals.items():
        write_output(args.output_file, key, str(value))
    for category, values in categories.items():
        write_output(args.output_file, f"{category}_suite_count", str(values["suites"]))
        write_output(args.output_file, f"{category}_tests", str(values["tests"]))

    suite_lines = "\n".join(
        f"| `{suite_name}` | {tests} |" for suite_name, tests in suites
    ) or "| No suites found | 0 |"
    category_lines = "\n".join(
        f"| {category.title()} | {values['suites']} | {values['tests']} |"
        for category, values in categories.items()
        if values["suites"] > 0
    ) or "| Other | 0 | 0 |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Unit and Integration Tests",
                "",
                "| Metric | Value |",
                "| --- | --- |",
                f"| Tests run | {totals['tests_run']} |",
                f"| Failures | {totals['failures']} |",
                f"| Errors | {totals['errors']} |",
                f"| Skipped | {totals['skipped']} |",
                f"| Duration | {format_duration(totals['duration_seconds'])} |",
                "",
                "| Suite | Tests |",
                "| --- | ---: |",
                suite_lines,
                "",
                "| Area | Suites | Tests |",
                "| --- | ---: | ---: |",
                category_lines,
                "",
            ]
        ),
    )
    return 0


def parse_karate_summary(args: argparse.Namespace) -> int:
    summary_path = Path(args.summary_json)
    if not summary_path.exists():
        write_output(args.output_file, "features_total", "0")
        write_output(args.output_file, "features_passed", "0")
        write_output(args.output_file, "features_failed", "0")
        write_output(args.output_file, "scenarios_total", "0")
        write_output(args.output_file, "scenarios_passed", "0")
        write_output(args.output_file, "scenarios_failed", "0")
        write_output(args.output_file, "elapsed_seconds", "0")
        append_summary(
            args.summary_file,
            "\n".join(
                [
                    "## Karate Contract Tests",
                    "",
                    "No Karate summary file was generated.",
                    "",
                ]
            ),
        )
        return 0

    data = json.loads(summary_path.read_text(encoding="utf-8"))
    features_total = data.get("featuresPassed", 0) + data.get("featuresFailed", 0) + data.get("featuresSkipped", 0)
    scenarios_total = data.get("scenariosPassed", 0) + data.get("scenariosfailed", 0)

    write_output(args.output_file, "features_total", str(features_total))
    write_output(args.output_file, "features_passed", str(data.get("featuresPassed", 0)))
    write_output(args.output_file, "features_failed", str(data.get("featuresFailed", 0)))
    write_output(args.output_file, "scenarios_total", str(scenarios_total))
    write_output(args.output_file, "scenarios_passed", str(data.get("scenariosPassed", 0)))
    write_output(args.output_file, "scenarios_failed", str(data.get("scenariosfailed", 0)))
    write_output(args.output_file, "elapsed_seconds", str(float(data.get("elapsedTime", 0)) / 1000.0))

    slowest = sorted(
        data.get("featureSummary", []),
        key=lambda feature: feature.get("durationMillis", 0),
        reverse=True,
    )[:3]
    slowest_lines = "\n".join(
        f"| `{feature.get('relativePath', 'n/a')}` | {feature.get('scenarioCount', 0)} | {format_duration(float(feature.get('durationMillis', 0)) / 1000.0)} |"
        for feature in slowest
    ) or "| No features found | 0 | 0.00s |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Karate Contract Tests",
                "",
                "| Metric | Value |",
                "| --- | --- |",
                f"| Features | {features_total} |",
                f"| Features failed | {data.get('featuresFailed', 0)} |",
                f"| Scenarios | {scenarios_total} |",
                f"| Scenarios failed | {data.get('scenariosfailed', 0)} |",
                f"| Duration | {format_duration(float(data.get('elapsedTime', 0)) / 1000.0)} |",
                "",
                "| Slowest feature | Scenarios | Duration |",
                "| --- | ---: | ---: |",
                slowest_lines,
                "",
            ]
        ),
    )
    return 0


def parse_gatling_console(args: argparse.Namespace) -> int:
    console_log = Path(args.console_log)
    if not console_log.exists():
        write_output(args.output_file, "requests_total", "0")
        write_output(args.output_file, "requests_ok", "0")
        write_output(args.output_file, "requests_ko", "0")
        write_output(args.output_file, "mean_response_ms", "n/a")
        write_output(args.output_file, "p95_ms", "n/a")
        write_output(args.output_file, "p99_ms", "n/a")
        write_output(args.output_file, "throughput_rps", "n/a")
        write_output(args.output_file, "assertions_total", "0")
        write_output(args.output_file, "assertions_failed", "0")
        write_output(args.output_file, "report_path", "")
        append_summary(
            args.summary_file,
            "\n".join(
                [
                    "## Gatling Performance Tests",
                    "",
                    "No Gatling console log was generated.",
                    "",
                ]
            ),
        )
        return 0

    text = console_log.read_text(encoding="utf-8", errors="replace")
    metrics: dict[str, str] = {}

    line_prefix_map = {
        "> request count": ("requests_total", 0),
        "> mean response time (ms)": ("mean_response_ms", 0),
        "> response time 95th percentile (ms)": ("p95_ms", 0),
        "> response time 99th percentile (ms)": ("p99_ms", 0),
        "> mean throughput (rps)": ("throughput_rps", 0),
    }

    for raw_line in text.splitlines():
        line = raw_line.strip()
        for prefix, (metric_key, column_index) in line_prefix_map.items():
            if line.startswith(prefix):
                columns = [column.strip().replace(",", "") for column in raw_line.split("|")[1:]]
                if len(columns) > column_index:
                    metrics[metric_key] = columns[column_index]

    request_line = next((line for line in text.splitlines() if line.strip().startswith("> request count")), "")
    if request_line:
        columns = [column.strip().replace(",", "") for column in request_line.split("|")[1:]]
        if len(columns) >= 3:
            metrics["requests_total"] = columns[0]
            metrics["requests_ok"] = columns[1]
            metrics["requests_ko"] = columns[2].replace("-", "0")

    assertion_results: list[tuple[str, str, str]] = []
    for raw_line in text.splitlines():
        match = re.match(r"^(.*) : (true|false) \(actual : (.*)\)$", raw_line.strip())
        if match:
            assertion_results.append((match.group(1), match.group(2), match.group(3)))

    metrics["assertions_total"] = str(len(assertion_results))
    metrics["assertions_failed"] = str(sum(1 for _, status, _ in assertion_results if status == "false"))

    report_match = re.search(r"Reports generated, please open the following file: file://(.*?/index\.html)", text)
    report_path = report_match.group(1) if report_match else ""
    metrics["report_path"] = report_path

    for key, value in metrics.items():
        write_output(args.output_file, key, value)

    failing_assertions = [
        f"| {name} | {actual} |"
        for name, status, actual in assertion_results
        if status == "false"
    ]
    assertion_lines = "\n".join(failing_assertions) or "| No failing assertions | n/a |"

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Gatling Performance Tests",
                "",
                "| Metric | Value |",
                "| --- | --- |",
                f"| Requests total | {metrics.get('requests_total', 'n/a')} |",
                f"| Requests OK | {metrics.get('requests_ok', 'n/a')} |",
                f"| Requests KO | {metrics.get('requests_ko', 'n/a')} |",
                f"| Mean response time | {metrics.get('mean_response_ms', 'n/a')} ms |",
                f"| P95 | {metrics.get('p95_ms', 'n/a')} ms |",
                f"| P99 | {metrics.get('p99_ms', 'n/a')} ms |",
                f"| Throughput | {metrics.get('throughput_rps', 'n/a')} rps |",
                f"| Failed assertions | {metrics.get('assertions_failed', '0')} / {metrics.get('assertions_total', '0')} |",
                "",
                "| Failing assertion | Actual value |",
                "| --- | --- |",
                assertion_lines,
                "",
            ]
            + ([f"Gatling report: `{report_path}`", ""] if report_path else [])
        ),
    )
    return 0


def parse_coverage_report(args: argparse.Namespace) -> int:
    report_path = Path(args.report_xml)
    threshold = float(args.minimum_ratio) * 100.0

    if not report_path.exists():
        write_output(args.output_file, "line_coverage_pct", "0.00")
        write_output(args.output_file, "covered_lines", "0")
        write_output(args.output_file, "missed_lines", "0")
        write_output(args.output_file, "coverage_threshold_pct", f"{threshold:.2f}")
        append_summary(
            args.summary_file,
            "\n".join(
                [
                    "## Coverage Quality Gate",
                    "",
                    "No JaCoCo report was generated.",
                    "",
                ]
            ),
        )
        return 0

    root = ET.parse(report_path).getroot()
    line_counter = next((counter for counter in root.findall("counter") if counter.attrib.get("type") == "LINE"), None)
    if line_counter is None:
        covered = 0
        missed = 0
    else:
        covered = int(line_counter.attrib.get("covered", "0"))
        missed = int(line_counter.attrib.get("missed", "0"))

    total = covered + missed
    coverage_pct = (covered / total * 100.0) if total else 0.0

    write_output(args.output_file, "line_coverage_pct", f"{coverage_pct:.2f}")
    write_output(args.output_file, "covered_lines", str(covered))
    write_output(args.output_file, "missed_lines", str(missed))
    write_output(args.output_file, "coverage_threshold_pct", f"{threshold:.2f}")

    append_summary(
        args.summary_file,
        "\n".join(
            [
                "## Coverage Quality Gate",
                "",
                "| Metric | Value |",
                "| --- | --- |",
                f"| Line coverage | {coverage_pct:.2f}% |",
                f"| Threshold | {threshold:.2f}% |",
                f"| Covered lines | {covered} |",
                f"| Missed lines | {missed} |",
                "",
            ]
        ),
    )
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Publish validation summaries for GitHub Actions.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    unit = subparsers.add_parser("unit")
    unit.add_argument("--reports-dir", required=True)
    unit.add_argument("--summary-file")
    unit.add_argument("--output-file")
    unit.set_defaults(handler=parse_unit_reports)

    karate = subparsers.add_parser("karate")
    karate.add_argument("--summary-json", required=True)
    karate.add_argument("--summary-file")
    karate.add_argument("--output-file")
    karate.set_defaults(handler=parse_karate_summary)

    gatling = subparsers.add_parser("gatling")
    gatling.add_argument("--console-log", required=True)
    gatling.add_argument("--summary-file")
    gatling.add_argument("--output-file")
    gatling.set_defaults(handler=parse_gatling_console)

    coverage = subparsers.add_parser("coverage")
    coverage.add_argument("--report-xml", required=True)
    coverage.add_argument("--minimum-ratio", required=True)
    coverage.add_argument("--summary-file")
    coverage.add_argument("--output-file")
    coverage.set_defaults(handler=parse_coverage_report)

    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    return args.handler(args)


if __name__ == "__main__":
    sys.exit(main())
