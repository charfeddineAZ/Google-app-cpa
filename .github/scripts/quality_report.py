#!/usr/bin/env python3
"""
Quality report generator for the CPA Automator Android CI.

Parses (whenever they exist):
  - Android Lint XML report   : app/build/reports/lint-results*.xml
  - JUnit unit-test XML       : app/build/test-results/**/*.xml
  - detekt TXT report         : app/build/reports/detekt/detekt.txt

and produces:
  - GitHub annotations (::error / ::warning) so failures point at the exact
    file/line directly in the pull-request "Files changed" view and the
    Actions "Problems" panel.
  - A Markdown summary appended to $GITHUB_STEP_SUMMARY.

The script never fails the job (exit 0): the Gradle steps themselves are the
gatekeepers. This only makes the results easier to read.
"""

import glob
import os
import re
import sys
import xml.etree.ElementTree as ET

REPO = os.environ.get("GITHUB_WORKSPACE", os.getcwd())
SUMMARY_FILE = os.environ.get("GITHUB_STEP_SUMMARY", "")

MAX_LINT_ERROR_ANNOTATIONS = 50
MAX_LINT_WARNING_ANNOTATIONS = 25
MAX_TEST_FAILURE_ANNOTATIONS = 30
MAX_DETEKT_ANNOTATIONS = 25

summary_lines = []


# --------------------------------------------------------------------------
# helpers
# --------------------------------------------------------------------------
def annotation(level, message, file=None, line=None, col=None, title=None):
    """Emit a GitHub workflow command annotation."""
    props = []
    if file:
        props.append("file=%s" % file)
    if line:
        props.append("line=%s" % line)
    if col:
        props.append("col=%s" % col)
    if title:
        props.append("title=%s" % _clean(title))
    message = _clean(message)
    if props:
        print("::%s %s::%s" % (level, ";".join(props), message))
    else:
        print("::%s::%s" % (level, message))


def _clean(text):
    """Single-line + percent-encode the characters GitHub reserves."""
    text = " ".join(str(text).split())
    return (
        text.replace("%", "%25")
        .replace("\r", "%0D")
        .replace("\n", "%0A")
        .replace(":", "%3A")
        .replace(",", "%2C")
    )


def summary(text):
    summary_lines.append(text)


def rel(path):
    """Absolute build paths -> repo-relative (nicer annotation links)."""
    if path and os.path.isabs(path) and path.startswith(REPO):
        return os.path.relpath(path, REPO)
    return path


# --------------------------------------------------------------------------
# Android Lint
# --------------------------------------------------------------------------
def parse_lint():
    paths = sorted(glob.glob("app/build/reports/lint-results*.xml"))
    if not paths:
        return None

    counts = {}
    error_ann = warning_ann = 0
    for xml_path in paths:
        try:
            root = ET.parse(xml_path).getroot()
        except ET.ParseError as exc:
            annotation("warning", "Could not parse lint report %s: %s" % (xml_path, exc))
            continue

        for issue in root.findall("issue"):
            severity = (issue.get("severity") or "").lower()
            counts[severity] = counts.get(severity, 0) + 1
            if severity not in ("error", "fatal", "warning"):
                continue

            location = issue.find("location")
            file_path = rel(location.get("file") or "") if location is not None else ""
            line = (location.get("line") if location is not None else "") or "1"
            col = location.get("column") if location is not None else ""

            message = "%s: %s" % (issue.get("id", "Lint"), issue.get("message", ""))

            if severity in ("error", "fatal"):
                if error_ann < MAX_LINT_ERROR_ANNOTATIONS:
                    annotation("error", message, file_path, line, col,
                               title="Android Lint: %s" % issue.get("id", ""))
                    error_ann += 1
            elif warning_ann < MAX_LINT_WARNING_ANNOTATIONS:
                annotation("warning", message, file_path, line, col,
                           title="Android Lint: %s" % issue.get("id", ""))
                warning_ann += 1

    return counts


# --------------------------------------------------------------------------
# Unit tests (JUnit XML)
# --------------------------------------------------------------------------
def parse_tests():
    paths = sorted(glob.glob("app/build/test-results/**/*.xml", recursive=True))
    if not paths:
        return None

    totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    failed_cases = []

    for xml_path in paths:
        try:
            root = ET.parse(xml_path).getroot()
        except ET.ParseError:
            continue

        for key in totals:
            try:
                totals[key] += int(root.get(key, 0) or 0)
            except ValueError:
                pass

        for testcase in root.iter("testcase"):
            problem = None
            for tag in ("failure", "error"):
                found = testcase.find(tag)
                if found is not None:
                    problem = (tag, found)
                    break
            if problem is not None:
                tag, node = problem
                failed_cases.append(
                    {
                        "class": testcase.get("classname", "?"),
                        "name": testcase.get("name", "?"),
                        "message": node.get("message") or (node.text or "").strip()[:400],
                        "tag": tag,
                    }
                )

    for case in failed_cases[:MAX_TEST_FAILURE_ANNOTATIONS]:
        annotation(
            "error",
            "[%s] %s %s" % (case["tag"].capitalize(), case["name"], case["message"]),
            title="Failed test: %s" % case["class"].rsplit(".", 1)[-1],
        )
    if len(failed_cases) > MAX_TEST_FAILURE_ANNOTATIONS:
        annotation(
            "error",
            "%d more failing tests are not listed here - see the quality-reports artifact"
            % (len(failed_cases) - MAX_TEST_FAILURE_ANNOTATIONS),
        )

    return totals, len(failed_cases)


# --------------------------------------------------------------------------
# detekt
# --------------------------------------------------------------------------
DETEKT_LINE = re.compile(r"^(.+?):(\d+):(\d+)\s*-\s*\[(\S+)\]\s*(.*)$")


def parse_detekt():
    path = "app/build/reports/detekt/detekt.txt"
    if not os.path.exists(path):
        return None

    findings = []
    with open(path, "r", encoding="utf-8", errors="replace") as handle:
        for line in handle:
            match = DETEKT_LINE.match(line.strip())
            if match:
                file_path, line_no, col, rule, message = match.groups()
                findings.append((rule, rel(file_path), line_no, message.strip()))

    for rule, file_path, line_no, message in findings[:MAX_DETEKT_ANNOTATIONS]:
        text = "detekt [%s] %s" % (rule, message) if message else "detekt [%s]" % rule
        annotation("warning", text, file_path, line_no, title="detekt: %s" % rule)
    if len(findings) > MAX_DETEKT_ANNOTATIONS:
        annotation(
            "warning",
            "%d more detekt findings - see the quality-reports artifact"
            % (len(findings) - MAX_DETEKT_ANNOTATIONS),
        )

    return findings


# --------------------------------------------------------------------------
# main
# --------------------------------------------------------------------------
def main():
    detekt_outcome = os.environ.get("DETEKT_OUTCOME", "")

    lint_counts = parse_lint()
    test_results = parse_tests()
    detekt_findings = parse_detekt()

    summary("## 🔎 Error detection report")
    summary("")

    # ---- lint table
    if lint_counts is None:
        summary("- 🟡 Android Lint: no report found (lint task did not run?)")
    else:
        errors = lint_counts.get("error", 0) + lint_counts.get("fatal", 0)
        warnings = lint_counts.get("warning", 0)
        infos = sum(v for k, v in lint_counts.items() if k not in ("error", "fatal", "warning"))
        icon = "✅" if errors == 0 else "❌"
        summary(
            "- %s Android Lint: **%d error(s)**, %d warning(s), %d info"
            % (icon, errors, warnings, infos)
        )

    # ---- tests table
    if test_results is None:
        summary("- 🟡 Unit tests: no results found (tests did not run?)")
    else:
        totals, failed = test_results
        passed = max(totals["tests"] - failed - totals["skipped"], 0)
        icon = "✅" if failed == 0 else "❌"
        summary(
            "- %s Unit tests: **%d failed** / %d passed / %d skipped"
            % (icon, failed, passed, totals["skipped"])
        )

    # ---- detekt table
    if detekt_findings is None:
        summary("- 🟡 detekt: no report found (static analysis skipped)")
    else:
        icon = "✅" if not detekt_findings else "⚠️"
        summary("- %s detekt: **%d finding(s)** (advisory)" % (icon, len(detekt_findings)))
        if detekt_outcome == "failure":
            summary("  <sub>detekt step reported issues (non-blocking)</sub>")

    summary("")
    summary(
        "<sub>Annotations appear inline in the PR diff and in the run's "
        "*Problems* tab. Full HTML/XML reports are in the `quality-reports` artifact.</sub>"
    )

    if SUMMARY_FILE:
        with open(SUMMARY_FILE, "a", encoding="utf-8") as handle:
            handle.write("\n".join(summary_lines) + "\n")

    # also print for the raw log
    print("\n".join(summary_lines))
    return 0


if __name__ == "__main__":
    sys.exit(main())
