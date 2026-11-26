# TemsSleep

TemsSleep is a sleep plugin for **Paper and Purpur 26.x (26.1.1+)** servers. It provides one-player sleep,
configurable sleep percentages, night-skip and clock animations, boss bars, insomnia, AFK exemptions,
per-world flags, and GSit/PlaceholderAPI/SuperVanish/Essentials hooks.

> A rebranded fork of [SleepMost](https://www.spigotmc.org/resources/60623/) by
> [MrGeneralQ](https://github.com/MrGeneralQ) and contributors, maintained by
> [Phantom6422](https://github.com/Phantom6422) for internal use.

## Install

Download the [latest release](https://github.com/Phantom6422/TemsSleep/releases/latest), place
`TemsSleep-<version>.jar` in `plugins/`, start the server, and edit `plugins/TemsSleep/config.yml` and
`messages.yml`.

Releases from **6.0.0 onward** (Paper/Purpur only) are kept on GitHub; older 5.x releases are removed. The
plugin's update checker reads the latest public release from `Phantom6422/TemsSleep`.

## Platforms

- Paper 26.x (26.1.1+)
- Purpur 26.x (26.1.1+)

Spigot, Bukkit and Folia are not supported.

## Commands

The main command is `/temssleep` with aliases `/ts`, `/sleepmost` and `/sm`.

| Command | Description |
| --- | --- |
| `/ts` | Show the command list |
| `/ts enable` / `/ts disable` | Enable or disable TemsSleep in your world |
| `/ts setflag <flag> <value>` | Change a sleep flag |
| `/ts info [page]` | Show your world's flags |
| `/ts version` | Show the installed and latest version |
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

Permissions use `temssleep.*` (for example `temssleep.setflag`, `temssleep.kick`, `temssleep.exempt`). Legacy
`sleepmost.*` nodes are also accepted.

## Build

```bash
./gradlew shadowJar
```

Requires JDK 25+. The shaded jar is written to `paper/build/libs/TemsSleep-<version>.jar`.

## Credits

- [MrGeneralQ](https://github.com/MrGeneralQ) — author of
  [SleepMost](https://www.spigotmc.org/resources/60623/)
- LesleyXYZ, HorrendousEntity, dubsector and the SleepMost contributors

## License

MIT. See [LICENSE](LICENSE).
