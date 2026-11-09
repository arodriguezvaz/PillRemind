#!/usr/bin/env python3
"""Estadísticas de descargas de las releases de PillRemind.

Consulta la API pública de GitHub y separa las descargas reales de los APK de
las comprobaciones de actualización (``update.json``). No usa dependencias
externas: solo la biblioteca estándar.

Uso:
    python3 scripts/release_stats.py
    python3 scripts/release_stats.py --json
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass
from typing import Any, Sequence

DEFAULT_REPOSITORY = "arodriguezvaz/PillRemind"
API_URL = "https://api.github.com/repos/{repository}/releases?per_page=100&page={page}"
MANIFEST_NAME = "update.json"
USER_AGENT = "PillRemind-release-stats"
BADGE_LABEL = "descargas APK"
BADGE_COLOR = "2ea44f"


@dataclass(frozen=True)
class ReleaseStats:
    """Descargas de una release publicada."""

    tag: str
    published_at: str
    apk_downloads: int
    manifest_downloads: int | None
    prerelease: bool

    @property
    def date(self) -> str:
        """Fecha de publicación en formato ISO corto."""
        return self.published_at[:10] if self.published_at else "-"


def fetch_releases(repository: str, token: str | None) -> list[dict[str, Any]]:
    """Descarga todas las releases publicadas, paginando la API."""
    releases: list[dict[str, Any]] = []
    page = 1
    while True:
        request = urllib.request.Request(
            API_URL.format(repository=repository, page=page)
        )
        request.add_header("Accept", "application/vnd.github+json")
        request.add_header("User-Agent", USER_AGENT)
        request.add_header("X-GitHub-Api-Version", "2022-11-28")
        if token:
            request.add_header("Authorization", f"Bearer {token}")

        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                payload = json.load(response)
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise SystemExit(
                f"Error de la API de GitHub ({error.code}): {detail}"
            ) from error
        except urllib.error.URLError as error:
            raise SystemExit(f"No se pudo conectar con GitHub: {error.reason}") from error

        if not isinstance(payload, list):
            raise SystemExit("La API de GitHub devolvió una respuesta inesperada.")

        releases.extend(payload)
        if len(payload) < 100:
            break
        page += 1

    return releases


def summarize(releases: list[dict[str, Any]]) -> list[ReleaseStats]:
    """Calcula las descargas de APK y de manifiesto de cada release."""
    result: list[ReleaseStats] = []
    for release in releases:
        # La API devuelve borradores si el token tiene permisos de escritura.
        if release.get("draft"):
            continue

        assets = release.get("assets") or []
        apk_downloads = sum(
            int(asset.get("download_count") or 0)
            for asset in assets
            if str(asset.get("name") or "").lower().endswith(".apk")
        )
        manifest = next(
            (asset for asset in assets if asset.get("name") == MANIFEST_NAME),
            None,
        )
        result.append(
            ReleaseStats(
                tag=str(release.get("tag_name") or "?"),
                published_at=str(release.get("published_at") or ""),
                apk_downloads=apk_downloads,
                manifest_downloads=(
                    int(manifest.get("download_count") or 0) if manifest else None
                ),
                prerelease=bool(release.get("prerelease")),
            )
        )
    return result


def latest_stable(stats: list[ReleaseStats]) -> ReleaseStats | None:
    """Devuelve la release estable más reciente, la que consulta la app."""
    return next((release for release in stats if not release.prerelease), None)


def badge_payload(stats: list[ReleaseStats], repository: str) -> dict[str, Any]:
    """Construye el JSON que consume el badge de shields.io."""
    total_apk = sum(release.apk_downloads for release in stats)
    total_manifest = sum(release.manifest_downloads or 0 for release in stats)
    stable = latest_stable(stats)

    return {
        "schemaVersion": 1,
        "label": BADGE_LABEL,
        "message": format_number(total_apk),
        "color": BADGE_COLOR,
        "cacheSeconds": 3600,
        "repository": repository,
        "totals": {
            "apk": total_apk,
            "manifest": total_manifest,
            "releases": len(stats),
        },
        "latestStable": {
            "tag": stable.tag if stable else None,
            "manifestDownloads": stable.manifest_downloads if stable else None,
        },
        "releases": [
            {
                "tag": release.tag,
                "publishedAt": release.published_at,
                "apkDownloads": release.apk_downloads,
                "manifestDownloads": release.manifest_downloads,
                "prerelease": release.prerelease,
            }
            for release in stats
        ],
    }


def format_number(value: int) -> str:
    """Formatea un número con puntos de millar (estilo español)."""
    return f"{value:,}".replace(",", ".")


def print_report(stats: list[ReleaseStats], repository: str) -> None:
    """Imprime el desglose en texto plano."""
    tags = [
        f"{release.tag}*" if release.prerelease else release.tag
        for release in stats
    ]
    tag_width = max([len("Release"), *(len(tag) for tag in tags)])

    print(f"Descargas de releases de {repository}")
    print()
    print(
        f"{'Release':<{tag_width}} {'Fecha':<10} {'APK':>5} {'update.json':>12}"
    )
    print(f"{'-' * tag_width} {'-' * 10} {'-' * 5} {'-' * 12}")
    for release, tag in zip(stats, tags):
        manifest = (
            str(release.manifest_downloads)
            if release.manifest_downloads is not None
            else "-"
        )
        print(
            f"{tag:<{tag_width}} {release.date:<10} "
            f"{release.apk_downloads:>5} {manifest:>12}"
        )
    print(f"{'-' * tag_width} {'-' * 10} {'-' * 5} {'-' * 12}")

    total_apk = sum(release.apk_downloads for release in stats)
    total_manifest = sum(release.manifest_downloads or 0 for release in stats)
    print(f"{'Total':<{tag_width}} {'':<10} {total_apk:>5} {total_manifest:>12}")
    print()
    print("APK: descargas del fichero, incluidas actualizaciones y pruebas propias.")
    print("update.json: comprobaciones de la app (máx. 1 cada 24 h por instalación).")

    stable = latest_stable(stats)
    if stable is not None:
        checks = (
            str(stable.manifest_downloads)
            if stable.manifest_downloads is not None
            else "sin update.json"
        )
        print(f"Última estable: {stable.tag} ({checks} comprobaciones).")
    if any(release.prerelease for release in stats):
        print("Las releases marcadas con * son preliminares.")


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    """Lee los argumentos de la línea de comandos."""
    parser = argparse.ArgumentParser(
        description="Descargas de las releases de PillRemind desde la API de GitHub."
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="emite el JSON que consume el badge de shields.io",
    )
    parser.add_argument(
        "--repository",
        default=DEFAULT_REPOSITORY,
        help=f"repositorio de GitHub (por defecto: {DEFAULT_REPOSITORY})",
    )
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    """Punto de entrada del script."""
    args = parse_args(argv)
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    stats = summarize(fetch_releases(args.repository, token))

    if args.json:
        json.dump(
            badge_payload(stats, args.repository),
            sys.stdout,
            ensure_ascii=False,
            indent=2,
        )
        sys.stdout.write("\n")
    else:
        print_report(stats, args.repository)
    return 0


if __name__ == "__main__":
    sys.exit(main())
