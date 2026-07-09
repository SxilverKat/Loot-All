# Publishing — one task, every jar, to Modrinth + CurseForge

This project can upload **every loader/version jar at once** to both Modrinth and CurseForge.

```bash
./gradlew chiseledPublish
```

That builds all nodes (forge/1.20.1, neoforge/1.21.1, fabric/1.20.1, fabric/1.21.1) and uploads each to both
platforms, tagged with the right Minecraft version, loader, Java version, and environment. Under the hood:
**Minotaur** (`com.modrinth.minotaur`) for Modrinth and **CurseForgeGradle** (`net.darkhax.curseforgegradle`)
for CurseForge.

## Task cheat-sheet
| Task | Does |
|---|---|
| `chiseledPublish` | build + publish **every** node to **both** platforms |
| `chiseledPublishFabric` | build + publish **only the Fabric** nodes (1.20.1 + 1.21.1); also `chiseledPublishForge`, `chiseledPublishNeoforge` |
| `<node>:publishMod` | publish one node to both (e.g. `:neoforge:publishMod`) |
| `<node>:modrinth` | one node → Modrinth only |
| `<node>:publishCurseForge` | one node → CurseForge only |

## One-time setup

1. **Create the projects** (once each):
   - Modrinth: make a project, copy its **Project ID** (Settings → General).
   - CurseForge: you already have `loot-all`; copy its numeric **Project ID** (right sidebar, "About Project").

2. **Fill the IDs** in `gradle.properties`:
   ```properties
   publish.modrinth_id=AbCdEfGh          # Modrinth id (leave blank to skip Modrinth)
   publish.curseforge_id=123456          # CurseForge id — MUST be numeric (blank = skip)
   ```

3. **Set the tokens as environment variables** — NEVER put tokens in gradle.properties or Git:
   - Modrinth: Account → PATs → create a token with `Create versions` scope → set `MODRINTH_TOKEN`.
   - CurseForge: https://legacy.curseforge.com/account/api-tokens → set `CURSEFORGE_TOKEN`.
   - Windows (PowerShell, current session):
     ```powershell
     $env:MODRINTH_TOKEN   = "mrp_xxx"
     $env:CURSEFORGE_TOKEN = "xxxxxxxx-xxxx-..."
     ```
     (For CI/GitHub Actions, add them as repository secrets instead.)

4. **Changelog** (optional): drop a `CHANGELOG.md` in the project root; its contents become the release changelog
   (markdown). If absent, a placeholder is used.

Then: `./gradlew chiseledPublish`.

## ⚠️ This uploads PUBLICLY — test first
Running `chiseledPublish` with tokens + IDs set makes **real, public releases**. Before the first real run:
- **Dry-run the build only:** `./gradlew chiseledBuild` (never uploads).
- **Test against Modrinth's staging server** by temporarily adding to a loader's `modrinth { }` block:
  `apiEndpoint.set("https://staging-api.modrinth.com/v2")` and using a staging token — uploads go to a throwaway
  environment, not the real site. Remove it for real releases.
- CurseForge has no staging; start with `releaseType = "alpha"` on a test project if you want to rehearse.

## Safety built in (won't break your build)
- **Tokens are lazy providers** — absent tokens don't fail configuration; only an actual upload run needs them.
- **CurseForge id is guarded** — the config calls `upload(...)` only if `publish.curseforge_id` is a valid number,
  so a blank/placeholder id can't break `build`/`chiseledBuild` (CurseForgeGradle parses the id at config time).
- With nothing configured, `chiseledPublish` simply does nothing for CurseForge and skips Modrinth (via `onlyIf`).

## Notes
- `versionType` is `release`; change per loader block if you want beta/alpha.
- `detectLoaders` is off so each jar is tagged with exactly its one loader + MC version.
- Each CurseForge file is tagged with its Minecraft version, loader, **Java version** (17 for 1.20.1, 21 for 1.21.1), and **environment** (Client + Server). Release type is `release`.
- The uploaded file is Loom's **remapJar** (the production jar), not the dev jar — correct by design.
