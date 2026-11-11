# TemsSleep

TemsSleep is a Paper/Spigot sleep plugin for Minecraft **1.21+** (including
**26.3**). It supports one-player sleep, configurable sleep percentages,
night-skip animations, clock animations, boss bars, insomnia, AFK exemptions,
GSit/PlaceholderAPI/SuperVanish hooks and more.

> **Note:** This plugin is a rebranded fork of [SleepMost](https://www.spigotmc.org/resources/60623/)
> by [MrGeneralQ](https://github.com/MrGeneralQ) and the SleepMost contributors.
> This fork targets Paper/Spigot 1.21+ and is maintained by
> [Phantom6422](https://github.com/Phantom6422) for
> [PhantomsNetwork](https://phantomsnetwork.org) internal use. We do not provide
> support for other servers.

> [!WARNING]
> **This fork is maintained for [PhantomsNetwork](https://phantomsnetwork.org)
> internal use only. We do not provide support for other servers.**
>
> **This plugin is heavily modified using AI / "vibe coding" methods.**
> The Paper 26.3 port, rebrand, GitHub update checker, release automation,
> command and permission changes, fixes and this documentation were produced with
> an AI coding agent under human direction and review.
>
> It is tested by building, by unit tests and by a Paper 26.3 startup smoke test,
> but it has **not** been battle-tested on a live server. Read the diff, test on a
> staging server, and keep a rollback jar before running it in production. Treat
> every change in here as "review before trusting".
>
> **Update checker note:** The plugin reads the latest **public** GitHub release.
> A private repository cannot be read by the plugin without credentials, so make
> `Phantom6422/TemsSleep` public (or point
> `update-checker-github-repository` in `config.yml` at a public repository)
> before relying on update checks.

## Requirements

- **Paper 1.21+** or **Spigot/Bukkit 1.21+**
- Java **21+**
- Tested on Paper **1.21.11** and Paper **26.3**
- Paper API-compatible forks that track those versions (for example Purpur or Pufferfish) are expected to work.
- Folia is not officially supported because TemsSleep uses the global Bukkit scheduler.

## Build

```bash
mvn clean package -pl paper -am
```

The shaded plugin is written to `target/TemsSleep-<version>.jar`.

## Install

1. Build the plugin or download the latest release.
2. Put `TemsSleep-<version>.jar` in your server's `plugins/` directory.
3. Start the server and configure `plugins/TemsSleep/config.yml`.

## Releases and updates

Releases are published from tags named `v<version>` (for example `v5.9.3`).
The plugin update checker reads the latest public GitHub release from
`Phantom6422/TemsSleep` by default. Change
`update-checker-github-repository` in `config.yml` if you use a different
repository.

## Commands

The main command is `/temssleep`. Backwards-compatible aliases are `/ts`,
`/sleepmost` and `/sm`.

| Command | Description |
| --- | --- |
| `/ts` | Show the command list |
| `/ts enable` / `/ts disable` | Enable/disable TemsSleep in your world |
| `/ts setflag <flag> <value>` | Change a sleep flag |
| `/ts info [page]` | Show the flags of your world |
| `/ts version` | Show the installed/latest version |
| `/ts reset` | Reset all flags in your world |
| `/ts setops` | Enable one-player sleep |
| `/ts bed` | Teleport to your bed spawn |
| `/ts reload` | Reload the configuration |
| `/ts sleep` | Toggle synthetic sleep |
| `/ts kick <player>` | Kick a sleeping player out of bed |
| `/ts insomnia` | Enable insomnia for the current night |
| `/ts getflag <flag>` | Read a flag value |
| `/ts resetflag <flag>` | Reset a flag value |
| `/ts debug` | Toggle debug output |

## Permissions

The primary permission nodes are `temssleep.*` (for example
`temssleep.setflag`, `temssleep.kick`, `temssleep.exempt`). The old
`sleepmost.*` nodes are still accepted for backwards compatibility.

## Credits

Based on SleepMost by MrGeneralQ, LesleyXYZ, HorrendousEntity, dubsector and
the SleepMost contributors.
