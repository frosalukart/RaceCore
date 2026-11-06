# RaceCore 2.0.0
Data-driven race plugin for Paper 26.2 (Java 25 server). Replaces RaceSystem.

## Build
Needs JDK 21+ and Maven. In this folder:   mvn clean package
Output: target/RaceCore-2.0.0.jar

## Install
1. Stop the server.
2. Back up plugins/RaceSystem/ and remove RaceSystem-2_0_0.jar from plugins/.
   (Both plugins use /race, /reroll etc., so they must not run together.)
3. Put RaceCore-2.0.0.jar in plugins/ and start the server.
   On first start it auto-imports plugins/RaceSystem/playerdata.yml.
   If it didn't, run /racesystem migrate.

## Add a race
Copy any section in plugins/RaceCore/races/default.yml (or make a new .yml
in that folder), then /racesystem reload.

## Crate / quest reward command
racemanage <player> set nephilim

## Placeholders
%race_name%  %race_id%  %race_rarity%  %race_rerolls%
