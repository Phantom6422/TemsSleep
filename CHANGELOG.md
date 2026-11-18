6.0.4 - Gradle build

- Convert the build from Maven to Gradle with root, core and paper modules
- Add a Gradle version catalog and wrapper
- Migrate tests to JUnit 5
- Build with the Java 25 toolchain
- Keep the shaded TemsSleep-<version>.jar output
- Update CI, README and local agent notes

6.0.3 - Paper/Purpur 26.x bug fixes

- Do not cancel daytime bed interactions that only set the spawn point
- Exclude exempt/AFK players from both the sleep requirement and the sleeper count
- Read /ts kick's per-world allow-kick flag from the target's world
- Support GSit 3.6+ pose names
- Unregister the command on disable
- Fix night-animation time checks and empty-sleeper night-skip handling
- Fix config defaults, legacy exemption, flag case, message prefix, and update-check warnings
- Derive plugin/config versions from Maven

6.0.2 - Full Paper/Purpur 26.x support

- Support the whole 26.x line from 26.1.1 onward
- Compile against Paper 26.1.2 and require api-version 26.1
- Detect 27.x+ and disable until it has been validated
- Add Paper/Purpur 26.x smoke-test matrices

6.0.1 - Newer Paper/Purpur versions

- Accept Paper/Purpur versions newer than 26.3 with a startup warning
- Keep 26.3-26.x on the tested API family

6.0.0 - Paper/Purpur 26.3 only

- Drop Spigot/Bukkit support and the spigot module
- Compile against Paper API 26.3 and require api-version 26.3
- Use native 26.3 game-rule and time-skip APIs
- Add Purpur detection and native Player#isAfk() support
- Add a Purpur 26.3 CI smoke test
