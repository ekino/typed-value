---
name: release
description: Prepare a Typed-Value release — analyze commits, generate release notes, update version references, and create PR
disable-model-invocation: true
argument-hint: "[version]"
---

# Release Skill for Typed-Value

You are preparing a release for the **Typed-Value** library. Follow these 8 steps in order. Do NOT skip any step. Ask for confirmation before proceeding to Step 8.

---

## Step 1: Validate Prerequisites

1. **Parse version argument**: The user must provide a version (e.g., `1.2.0`). If missing, ask for it. Validate it follows semver (`MAJOR.MINOR.PATCH` or with pre-release suffix like `-rc.1`).

2. **Check current branch**: Must be `main`. If not, abort with a message.
   ```bash
   git branch --show-current
   ```

3. **Check for clean working tree**: No uncommitted changes allowed (ignore untracked files).
   ```bash
   git status --porcelain
   ```

4. **Check that the tag does not already exist**:
   ```bash
   git tag -l "v{VERSION}"
   ```

5. **Check that the release branch does not already exist**:
   ```bash
   git branch -a | grep "release/v{VERSION}"
   ```

6. **Fetch latest from remote**:
   ```bash
   git fetch origin main --tags
   ```

7. **Check local is up to date with remote**:
   ```bash
   git diff origin/main --stat
   ```

If any check fails, stop and explain what needs to be fixed.

---

## Step 2: Analyze Commits Since Last Tag

1. Find the last release tag:
   ```bash
   git describe --tags --abbrev=0
   ```

2. List all commits since that tag:
   ```bash
   git log {LAST_TAG}..HEAD --oneline --no-merges
   ```

3. Also list merge commits (for PR context):
   ```bash
   git log {LAST_TAG}..HEAD --oneline --merges
   ```

4. Categorize commits into:
   - **Features** (`feat:` or new functionality)
   - **Bug Fixes** (`fix:` or bug corrections)
   - **Dependencies** (`chore(deps)` or dependency bumps)
   - **Build & Infrastructure** (CI, Gradle, build changes)
   - **Documentation** (`docs:` or documentation changes)
   - **Refactoring** (`refactor:` or code improvements)
   - **Other** (anything that doesn't fit above)

5. Display the categorized summary to the user.

---

## Step 3: Determine Versions

Set these three variables:

| Variable | Value | Example |
|----------|-------|---------|
| `NEW_VERSION` | The version being released | `1.2.0` |
| `OLD_VERSION` | The version from the last tag (without `v` prefix) | `1.1.0` |
| `NEXT_SNAPSHOT` | Next development snapshot: bump patch +1, add `-SNAPSHOT` | `1.2.1-SNAPSHOT` |

**NEXT_SNAPSHOT logic:**
- For `1.2.0` → `1.2.1-SNAPSHOT`
- For `2.0.0` → `2.0.1-SNAPSHOT`
- For `1.2.0-rc.1` → `1.2.0-SNAPSHOT` (strip pre-release, keep version)

Display all three values for confirmation.

---

## Step 4: Generate Release Notes

Create file: `release-notes/RELEASE_NOTES_v{NEW_VERSION}.md`

**Before writing, read existing release notes to calibrate tone and format:**

- For **patch/dependency-only releases** → follow the style of `release-notes/RELEASE_NOTES_v1.0.1.md` (concise, ~35 lines)
- For **feature releases** → follow the style of `release-notes/RELEASE_NOTES_v1.1.0.md` (detailed, ~200 lines)

**Required sections** (adapt depth based on release type):

1. **Title**: `# Typed-Value v{NEW_VERSION}`
2. **Description**: One-line summary of the release
3. **What's Changed / What's New**: Categorized changes from Step 2
4. **Installation**: Dependency coordinates with `{NEW_VERSION}`
   ```kotlin
   implementation("com.ekino.oss:typed-value-core:{NEW_VERSION}")
   ```
   Include all published modules for feature releases; core + common ones for patches.
5. **Links**: Documentation, GitHub, Maven Central
6. **Full Changelog**: `https://github.com/ekino/typed-value/compare/v{OLD_VERSION}...v{NEW_VERSION}`

---

## Step 5: Update Version References

Update exactly **4 files**. Do NOT modify any other files.

### File 1: `release-notes/RELEASE_NOTES_v{NEW_VERSION}.md`
Already created in Step 4. No further changes needed.

### File 2: `README.md`
Replace all occurrences of the old version in dependency coordinates:
```
com.ekino.oss:typed-value-core:{OLD_VERSION}  →  com.ekino.oss:typed-value-core:{NEW_VERSION}
com.ekino.oss:typed-value-jackson:{OLD_VERSION}  →  com.ekino.oss:typed-value-jackson:{NEW_VERSION}
com.ekino.oss:typed-value-spring:{OLD_VERSION}  →  com.ekino.oss:typed-value-spring:{NEW_VERSION}
```
Search for all `:{OLD_VERSION}` patterns and replace with `:{NEW_VERSION}`.

### File 3: `build.gradle.kts`
Update the SNAPSHOT fallback version (line ~50):
```kotlin
// Before:
else -> project.findProperty("localVersion") as String? ?: "{OLD_VERSION}-SNAPSHOT"

// After:
else -> project.findProperty("localVersion") as String? ?: "{NEXT_SNAPSHOT}"
```

### File 4: `docs/.vitepress/versions.data.ts`
Update the `typedValue` version. Do NOT change the `kotlin` version:
```typescript
// Before:
typedValue: '{OLD_VERSION}',

// After:
typedValue: '{NEW_VERSION}',
```

**After updating, display a summary of all changes.**

---

## Step 6: Run Build Checks

Run formatting and full build:

```bash
./gradlew spotlessApply
./gradlew clean build
```

If `spotlessApply` modifies files, that's normal — those formatting changes will be included in the commit.

If `build` fails, stop and investigate. Do NOT proceed with a broken build.

---

## Step 7: Review & Confirm

Present a summary to the user:

```
Release Summary for Typed-Value v{NEW_VERSION}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Version : {OLD_VERSION} → {NEW_VERSION}
Snapshot: {NEXT_SNAPSHOT}

Files modified (4):
  1. release-notes/RELEASE_NOTES_v{NEW_VERSION}.md  (new)
  2. README.md                                       (version bump)
  3. build.gradle.kts                                (snapshot bump)
  4. docs/.vitepress/versions.data.ts                (version bump)

Build: ✅ Passed
```

Show `git diff --stat` and offer to show the full diff if requested.

**Ask the user to confirm before proceeding to Step 8.**

---

## Step 8: Create PR

1. **Create release branch**:
   ```bash
   git checkout -b release/v{NEW_VERSION}
   ```

2. **Stage all changed files**:
   ```bash
   git add release-notes/RELEASE_NOTES_v{NEW_VERSION}.md README.md build.gradle.kts docs/.vitepress/versions.data.ts
   ```

3. **Commit**:
   ```bash
   git commit -m "chore(release): prepare v{NEW_VERSION}"
   ```

4. **Push branch**:
   ```bash
   git push -u origin release/v{NEW_VERSION}
   ```

5. **Create PR**:
   ```bash
   gh pr create \
     --title "chore(release): prepare v{NEW_VERSION}" \
     --body "$(cat <<'EOF'
   ## Release v{NEW_VERSION}

   ### Changes
   - Updated version references to `{NEW_VERSION}`
   - Updated development snapshot to `{NEXT_SNAPSHOT}`
   - Added release notes

   ### Files Modified
   1. `release-notes/RELEASE_NOTES_v{NEW_VERSION}.md` — new release notes
   2. `README.md` — dependency coordinates updated
   3. `build.gradle.kts` — snapshot version bumped
   4. `docs/.vitepress/versions.data.ts` — docs version updated

   ### Post-Merge Steps
   After merging this PR, create and push the release tag to trigger CI publishing:
   ```bash
   git checkout main && git pull
   git tag v{NEW_VERSION} && git push origin v{NEW_VERSION}
   ```

   This will trigger the `publish.yml` workflow which:
   - Builds and tests all modules
   - Publishes to Maven Central
   - Creates a GitHub Release with the release notes
   - Uploads build artifacts

   Monitor: [GitHub Actions](https://github.com/ekino/typed-value/actions)
   EOF
   )"
   ```

6. **Display the PR URL** and remind the user of the post-merge steps:

   ```
   ✅ PR created: {PR_URL}

   After the PR is merged, run:
     git checkout main && git pull
     git tag v{NEW_VERSION} && git push origin v{NEW_VERSION}

   Then monitor:
     - GitHub Actions: https://github.com/ekino/typed-value/actions
     - Maven Central: https://central.sonatype.com/search?q=com.ekino.oss.typed-value
     - GitHub Releases: https://github.com/ekino/typed-value/releases
   ```
