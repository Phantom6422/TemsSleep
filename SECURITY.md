# Security Policy

TemsSleep is maintained for **internal use only**. Public support is not provided. Security reports are still welcome and will be handled privately.

## Reporting a vulnerability

Please do **not** open a public GitHub issue, pull request, or Discord channel message for a security problem.

Report it privately through either channel:

- **Email:** [mail@phantomsnetwork.org](mailto:mail@phantomsnetwork.org)
- **Discord:** [https://phantomsnetwork.org/discord](https://phantomsnetwork.org/discord) — contact a PhantomsNetwork administrator privately; do not post details in a public channel.

Please include:

- the affected TemsSleep version,
- the Paper/Purpur version,
- the smallest reproduction you can share,
- the impact you observed (permission bypass, crash, data exposure, etc.),
- and any suggested fix.

Do not include real player data, credentials, tokens, IP addresses or production addresses. Use a test server and dummy data where possible.

## What to expect

- Acknowledgment of the report as soon as practical.
- Private triage and a fix in a future release.
- Coordinated disclosure only after a fix is available. Please do not publish details before then.
- No bug bounty is offered.

## Scope

TemsSleep is a Paper/Purpur server plugin. Relevant boundaries include:

- command and permission checks (`temssleep.*` and legacy `sleepmost.*`),
- per-world configuration and flag handling,
- sleep-count and exemption logic,
- PlaceholderAPI, GSit, SuperVanish/PremiumVanish and Essentials hooks,
- the optional update checker.

The following are out of scope:

- vulnerabilities in Paper, Purpur, the JDK, GSit, PlaceholderAPI, SuperVanish, Essentials or other third-party software (report those upstream),
- issues that require an already-compromised server console or filesystem,
- players bypassing sleep by using another plugin or administrative command,
- support requests, configuration help or feature requests.
