# SimplyFist
by Yorinu

**SimplyFist** is a custom Minecraft Forge mod for Minecraft 1.20.1 that introduces specialized fist weapons with unique combat mechanics, passive effects, and active abilities.

Instead of relying solely on conventional swords and axes, SimplyFist focuses on close-range combat through specialized gauntlets and knuckle weapons, each designed to provide a distinct fighting style.

## Features

* **Unique Fist Weapons** — A collection of specialized weapons with individual combat mechanics.
* **Custom Combat Effects** — Status effects and combat interactions designed specifically for the mod.
* **Passive Abilities** — Weapon-specific effects triggered by attacks, consecutive hits, or combat conditions.
* **Active Abilities** — Special attacks and abilities activated through player input.
* **Custom Effects and Particles** — Visual feedback and custom status effects to make combat more engaging.
* **Configurable Foundation** — A project structure designed to support future configuration options and additional mechanics.

## Weapons

### Boxing Gloves

A close-combat weapon built around rapid attacks and disruption, with a chance to stun enemies and a mobility-focused active ability.

### Bloody Knuckles

A brutal melee weapon featuring a bleeding mechanic that rewards repeated hits, alongside a health-sacrifice ability that grants vampirism.

### Mishima Gauntlets

A specialized gauntlet planned as part of the mod's expanding collection of unique fist weapons.

### Thunder Spear Gauntlet

A combat gauntlet featuring a stacking movement-speed passive and a powerful charged Superman Punch. Defeating enemies with the special attack can increase its accumulated bonus damage.

### Fire Spear Gauntlet

A specialized gauntlet planned for the mod's collection of elemental and ability-focused weapons.

### Sterak's Gage

A defensive and offensive fist weapon featuring **Deathbringer Stance**, which deals additional damage based on a target's current health on every fourth qualifying hit. Its active ability, **Lifeline**, grants temporary absorption health.

*Weapon mechanics may evolve as development continues.*

## Custom Status Effects

SimplyFist includes custom status effects that support its weapon abilities, including:

* **Stun** — Disrupts an affected entity's ability to fight effectively.
* **Bleeding** — Applies additional damage through a weapon-specific effect.
* **Vampirism** — Supports health-related combat mechanics.
* **Rising Strike** — A custom effect associated with the mod's combat system.

## Technical Information

| Property          | Details                         |
| ----------------- | ------------------------------- |
| Mod Name          | SimplyFist                      |
| Mod ID            | `simplyfist`                    |
| Minecraft Version | 1.20.1                          |
| Mod Loader        | Minecraft Forge                 |
| Forge Version     | 47.4.20 development environment |
| Java Version      | Java 17                         |
| Language          | Java                            |
| IDE               | IntelliJ IDEA                   |

## Development

SimplyFist is developed as a Java-based Forge mod. Its codebase separates weapon implementations, item registration, custom effects, sounds, and configuration infrastructure to make the project easier to maintain and extend.

### Building the Project

1. Install a compatible Java 17 JDK.
2. Clone or download the repository.
3. Open the project in IntelliJ IDEA or another Gradle-compatible IDE.
4. Allow Gradle to synchronize and download the required dependencies.
5. Build the project using the Gradle wrapper.

On Windows, run the following command from the project root:

```bat
gradlew.bat build
```

The resulting mod JAR should be generated in:

```text
build/libs/
```

Use a compatible Minecraft Forge 1.20.1 installation to test the compiled mod.

## Planned Development

SimplyFist is intended to expand with additional weapons, more specialized combat abilities, improved visual and audio effects, and further balancing and compatibility improvements.

## Copyright and Intellectual Property

**Copyright © 2026 Karl Angelo P. Dela Cruz. All Rights Reserved.**

Unless explicitly authorized in writing by the copyright holder, no license or permission is granted to reproduce, redistribute, modify, republish, or create derivative works from the original SimplyFist source code, original assets, documentation, or other project materials.

The project is published for viewing and development reference. Public availability of this repository does not, by itself, grant permission to reuse its contents outside the rights provided by applicable law and the hosting platform's terms.

The copyright holder retains all rights to original project materials, subject to applicable law and the rights of third-party authors.

Third-party software, libraries, assets, trademarks, and other materials remain subject to their respective owners' rights and applicable licenses.

**No open-source license is granted by this README.** Unless a separate license is explicitly provided, all rights in the original project materials are reserved.

## Disclaimer

SimplyFist is an independent fan-made Minecraft modification and is not an official Minecraft, Mojang, or Microsoft product. Minecraft and associated trademarks belong to their respective owners.

## Author

**Karl Angelo P. Dela Cruz**

Developer of SimplyFist.
