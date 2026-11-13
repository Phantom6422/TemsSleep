# TemsSleep

TemsSleep is a sleep plugin for Paper, Spigot and Bukkit Minecraft servers (**1.21+**, tested up to **26.3**)
with one-player sleep, configurable sleep percentages, night-skip animations, clock animations, boss bars,
insomnia, AFK exemptions and GSit/PlaceholderAPI/SuperVanish/Essentials hooks.

> **Note:** This plugin is a rebranded fork of [SleepMost](https://www.spigotmc.org/resources/60623/) by
> [MrGeneralQ](https://github.com/MrGeneralQ) and the SleepMost contributors. This fork is maintained by
> [Phantom6422](https://github.com/Phantom6422) for [PhantomsNetwork](https://phantomsnetwork.org) internal
> use, so we do not provide support for other servers.

> [!WARNING]
> **This plugin is heavily modified using AI / "vibe coding" methods.**
>
> The Paper 26.3 port, rebrand, GitHub update checker, release automation, command and permission changes,
> fixes and this documentation were produced with an AI coding agent under human direction and review.
>
> It is tested by building, by unit tests and by a Paper 26.3 startup smoke test, but it has **not** been
> battle-tested on a live server. Read the diff, test on a staging server, and keep a rollback jar before
> running it in production. Treat every change in here as "review before trusting".

## Quick information

<img src="https://img.shields.io/badge/Java%20version-%2021+-blue?style=for-the-badge&logo=java&logoColor=white"
alt="Plugin requires Java 21 or newer"></img>
<img src="https://img.shields.io/badge/Minecraft-%201.21%2B-green?style=for-the-badge&logo=minecraft&logoColor=white"
alt="Supports Minecraft 1.21 and newer"></img>

## Documentation

TemsSleep is configured through `plugins/TemsSleep/config.yml` and `plugins/TemsSleep/messages.yml`.
Most options can also be changed in-game per world with `/ts setflag`; see the
[Commands and permissions](#commands-and-permissions) section below.

To install, build the plugin or download the latest release, put `TemsSleep-<version>.jar` in your
server's `plugins/` directory, start the server and edit the generated configuration files.

## Basic set of features

- One-player sleep and configurable sleep percentages
- Night-skip and clock animations through to dawn
- Boss bar progress with customizable messages and titles
- Insomnia and AFK exemptions
- GSit synthetic sleep, PlaceholderAPI placeholders, SuperVanish/PremiumVanish and Essentials hooks
- Per-world flags and configuration
- Update checker against public GitHub releases

## Considerations

- Paper and Spigot/Bukkit are supported from the same shaded jar.
- Folia is not officially supported because TemsSleep uses the global Bukkit scheduler.
- Paper API-compatible forks that track 1.21+ (for example Purpur or Pufferfish) are expected to work.
- The update checker reads the latest **public** GitHub release. A private repository cannot be read by the
  plugin without credentials, so keep `Phantom6422/TemsSleep` public (or point
  `update-checker-github-repository` in `config.yml` at a public repository) before relying on update checks.

## Platforms

- [x] Paper (1.21+, tested up to 26.3)
- [x] Spigot / Bukkit (1.21+)
- [ ] Folia *(Uses the global Bukkit scheduler)*

## Commands and permissions

The main command is `/temssleep`. Backwards-compatible aliases are `/ts`, `/sleepmost` and `/sm`.

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

The primary permission nodes are `temssleep.*` (for example `temssleep.setflag`, `temssleep.kick` and
`temssleep.exempt`). The old `sleepmost.*` nodes are still accepted for backwards compatibility.

## Releases and updates

Releases are published from tags named `v<version>` (for example `v5.9.6`). The plugin update checker reads
the latest public GitHub release from `Phantom6422/TemsSleep` by default. Change
`update-checker-github-repository` in `config.yml` if you use a different repository.

## Building from Source

```bash
mvn clean package -pl paper -am
```

Requires JDK 25 or newer; the produced bytecode targets Java 21. The shaded plugin is written to
`target/TemsSleep-<version>.jar`.

## Special thanks

- [MrGeneralQ](https://github.com/MrGeneralQ) - author of [SleepMost](https://www.spigotmc.org/resources/60623/),
  which this plugin is based on
- LesleyXYZ, HorrendousEntity, dubsector and the SleepMost contributors
- [PhantomsNetwork](https://phantomsnetwork.org) - for testing and running TemsSleep

## License

TemsSleep is licensed under the MIT License. The original project credits and notices are kept in the
repository.

- [License text](LICENSE)
- Upstream project: [SleepMost](https://www.spigotmc.org/resources/60623/)