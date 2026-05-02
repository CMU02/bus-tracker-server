# [Git Commit Message Naming Convention]

## WARNING: Do not commit to Git under any circumstances until you are specifically instructed to do so.

## 1. Basic Structure

- `feat`: Used when adding a new feature

- `fix`: Used when fixing a bug

- `docs`: Used when making documentation-only changes (README, comments, Wiki, etc.)

- `style`: Used for style or format changes that don't affect functionality (e.g., missing semicolons, white spaces)

- `refactor`: Used when refactoring code without changing its behavior

- `chore`: Used for routine or miscellaneous tasks (build scripts, package manager config, lint settings)

- `perf`: Used when improving performance

- `ci`: Used for CI (Continuous Integration) configuration or script modifications

- `release`: Used when releasing a new version or tagging a release

## 2. Commit Message Format

```text
<type>(optional: scope): <title>

(optional) Body

(optional) Footer (e.g., issue tracking number, breaking change notice)
```

1. Do not end the title with a period.

2. Include a detailed body if necessary to describe the changes.

3. If applicable, reference related issues in the footer (e.g., Closes #123).

## 3. Commit by Logical Units

> Please divide your current work into logical units and commit them separately.

### Steps

1. Check current changes
   - Use git status to check staged/unstaged changes.

   - Use git diff to review detailed changes in each file.

2. Group changes by logical unit
   - Group related changes together.

   - Each group should represent a complete, meaningful unit of work.

   - Classify the commits based on the basic structure above.

3. Commit each unit sequentially
   - Start with the most essential changes.

   - For each unit:

   - `git add [related files]`

   - Write the commit message following the naming convention and rules.

4. Verify commit completion
   - Run `git log --oneline -10` to check recent commit history.

   - Run `git status` to ensure no remaining changes.

### Notes

- Each commit should be meaningful and self-contained.

- Unrelated changes must be committed separately.

- Never commit sensitive files (e.g., .env, credentials).

- Unless instructed otherwise, write commit messages in Korean by default.

## 4. Pull Requests (PR)

- Keep PRs small and focused on a single task.

- Provide a clear description and screenshots (if UI related).

- Ensure all CI checks pass before requesting a review.
