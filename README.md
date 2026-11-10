# TemsSleep

TemsSleep is a Paper sleep plugin for Minecraft **26.3**. It supports one-player
sleep, configurable sleep percentages, night-skip animations, clock animations,
boss bars, insomnia, AFK exemptions, GSit/PlaceholderAPI/SuperVanish hooks and
more.

TemsSleep is a rebranded fork of the original **SleepMost** project by
MrGeneralQ and contributors.

## Requirements

- Paper **26.3** or newer
- Java **25**

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

Releases are published from tags named `v<version>` (for example `v5.8.0`).
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
