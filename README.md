# LambDynamicLights

<!-- modrinth_exclude.start -->
![Java 21](https://img.shields.io/badge/language-Java%2021-9115ff.svg?style=flat-square) <!-- modrinth_exclude.end -->
[![GitHub license](https://img.shields.io/badge/license-Lambda%20License-c7136d?style=flat-square)](https://raw.githubusercontent.com/LambdAurora/LambDynamicLights/1.19/LICENSE)
![Environment: Client](https://img.shields.io/badge/environment-client-1976d2?style=flat-square)
[![Mod loader: Fabric]][fabric] <!-- modrinth_exclude.start -->
![Version](https://img.shields.io/github/v/tag/LambdAurora/LambDynamicLights?label=version&style=flat-square)
[![CurseForge](http://cf.way2muchnoise.eu/title/393442.svg)](https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights)
<!-- modrinth_exclude.end -->

A dynamic lights mod for Quilt and Fabric.

## 📖 What's this mod?

It's dark outside, extremely dark...
You can't see a lot in the darkness, you wish you had a torch,
or a lantern to hold and see in the light...

And this is now possible with this mod as it adds dynamic lights to the game.
You can see in the darkness thanks to your torch now!

This mod adds dynamic lights to Minecraft. Dynamic lights are lights created by an entity holding an
item which makes light as a block, or created by an entity on fire, etc.

[Trailer](https://www.youtube.com/embed/r8r1TNG45tM?wmode=transparent)

Searching other mods to replace OptiFine? [Check out this list!](https://optifine.alternatives.lambdaurora.dev/)

## ✅ Features:

- Dynamic lights.
- Settings to select how smooth the dynamic lighting is.
- Works with some modded items.
- Any entity holding an item which emits light will emit light!
- Magma cubes emit light.
- Spectral arrows emit light.
- Burning entities emit light.
- Blazes emit light.
- Some items like torches, soul torches, etc. will not light up in water.
- Quick and simple API for developers.
- And more!

## Screenshots

### Items emit light

Dropped items which already emit light as a block, will also dynamically emit light!

![Torch](https://media.forgecdn.net/attachments/301/21/2020-07-04_22.png)

### Held items emit light

Light is emitted when entities hold light emitting items.

![Fox holding lantern](https://media.forgecdn.net/attachments/301/22/2020-07-04_22.png)

### Fire! Fire! Fire!

Any entity on fire will emit light!

![Skeleton on fire!](https://media.forgecdn.net/attachments/301/23/2020-07-04_22.png)

### Spectral arrows

Spectral arrows will emit a very weak light!

![Spectral arrows](https://media.forgecdn.net/attachments/301/25/2020-07-04_22.png)

### Different luminance!

Light emitted from items depend on the light emitted from their respective blocks!

![light levels](https://media.forgecdn.net/attachments/301/26/2020-07-04_22.png)

### Configuration GUI

As of v2.1.0.

![Configuration GUI](images/settings_main.png)
![Entities Configuration](images/settings_entities.png)

## 📖 Usage

Using this mod is very simple!

Install it in your mods folder along with [Fabric API] and [ModMenu], (and [Sodium] if wanted for better performances).

You will notice nothing at first but if you go into the video options or into the settings screen of the mod via [ModMenu], you will notice an option called Dynamic Lights which is by default off, choose the wanted configuration and enjoy!
You can also configure the mod by editing the file in `config/lambdynlights.toml`.

Need help? Come check out [my Discord server][LambdAurora Discord].

<!-- modrinth_exclude.start -->
### Build

Just do `./gradlew build` and everything should build just fine!
<!-- modrinth_exclude.end -->

## 📖 How does it work internally?

Check [this documentation](https://github.com/LambdAurora/LambDynamicLights/blob/1.17/HOW_DOES_IT_WORK.md).

## 📖 Is there an API? How to use it as a developer?

Check [this documentation](https://github.com/LambdAurora/LambDynamicLights/blob/1.17/API.md).

# 📖 Compatibility

- [Sodium] is recommended for better performances.
- [Canvas] is compatible but still WIP: expect huge lag spike with it until a proper lighting API is done in Canvas.
- **OptiFabric is obviously incompatible.**


**<p style="color: red">Please, when you write the name of this mod, don't add spaces.</p>**

[fabric]: https://fabricmc.net
[Mod loader: Fabric]: https://img.shields.io/badge/modloader-Fabric-1976d2?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFHGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDIgNzkuMTYwOTI0LCAyMDE3LzA3LzEzLTAxOjA2OjM5ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDE4LTEyLTE2VDE2OjU0OjE3LTA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgZGM6Zm9ybWF0PSJpbWFnZS9wbmciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHBob3Rvc2hvcDpJQ0NQcm9maWxlPSJzUkdCIElFQzYxOTY2LTIuMSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIj4gPHhtcE1NOkhpc3Rvcnk+IDxyZGY6U2VxPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY3JlYXRlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHN0RXZ0OndoZW49IjIwMTgtMTItMTZUMTY6NTQ6MTctMDg6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE4IChXaW5kb3dzKSIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4/HiGMAAAAtUlEQVRYw+XXrQqAMBQF4D2P2eBL+QIG8RnEJFaNBjEum+0+zMQLtwwv+wV3ZzhhMDgfJ0wUSinxZUQWgKos1JP/AbD4OneIDyQPwCFniA+EJ4CaXm4TxAXCC0BNHgLhAdAnx9hC8PwGSRtAFVMQjF7cNTWED8B1cgwW20yfJgAvrssAsZ1cB3g/xckAxr6FmCDU5N6f488BrpCQ4rQBJkiMYh4ACmLzwOQF0CExinkCsvw7vgGikl+OotaKRwAAAABJRU5ErkJggg==
[Fabric API]: https://www.curseforge.com/minecraft/mc-mods/fabric-api "Fabric API CurseForge page"
[ModMenu]: https://modrinth.com/mod/modmenu
[Sodium]: https://www.curseforge.com/minecraft/mc-mods/sodium "Sodium CurseForge page"
[Canvas]: https://www.curseforge.com/minecraft/mc-mods/canvas-renderer "Canvas CurseForge page"
[LambdAurora Discord]: https://discord.gg/abEbzzv
