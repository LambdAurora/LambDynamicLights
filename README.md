# LambDynamicLights

<!-- modrinth_exclude.start -->
![Java 21](https://img.shields.io/badge/language-Java%2021-9115ff.svg?style=flat-square) <!-- modrinth_exclude.end -->
[![GitHub license](https://img.shields.io/badge/license-Lambda%20License-c7136d?style=flat-square)](https://raw.githubusercontent.com/LambdAurora/LambDynamicLights/1.19/LICENSE)
![Environment: Client](https://img.shields.io/badge/environment-client-1976d2?style=flat-square)
[![Mod loader: Fabric]][fabric] <!-- modrinth_exclude.start -->
![Version](https://img.shields.io/github/v/tag/LambdAurora/LambDynamicLights?label=version&style=flat-square)
[![CurseForge](http://cf.way2muchnoise.eu/title/393442.svg)](https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights)
<!-- modrinth_exclude.end -->

A dynamic lighting mod for Fabric.

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

- Dynamic lighting.
- Settings to select how smooth the dynamic lighting is.
- Works with some modded items.
- Any entity holding an item which emits light will emit light!
- Magma cubes emit light.
- Spectral arrows emit light.
- Burning entities emit light.
- Blazes emit light.
- Some items like torches, soul torches, etc. will not light up in water.
- Quick and simple API for developers and resource pack makers.
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

<!-- modrinth_exclude.long_start -->
## Downloads

<div>
<a href="https://modrinth.com/mod/lambdynamiclights" style="display: inline-flex; align-items: center;">
<svg aria-hidden="true" viewBox="16 -2 150 150" data-view-component="true" width="42" height="42"><path fill-rule="evenodd" fill="#1bd96a" d="M159.07,89.29A70.94,70.94,0,1,0,20,63.52H32A58.78,58.78,0,0,1,145.23,49.93l-11.66,3.12a46.54,46.54,0,0,0-29-26.52l-2.15,12.13a34.31,34.31,0,0,1,2.77,63.26l3.19,11.9a46.52,46.52,0,0,0,28.33-49l11.62-3.1A57.94,57.94,0,0,1,147.27,85Z"></path><path fill-rule="evenodd" fill="#1bd96a" d="M108.92,139.3A70.93,70.93,0,0,1,19.79,76h12a59.48,59.48,0,0,0,1.78,9.91,58.73,58.73,0,0,0,3.63,9.91l10.68-6.41a46.58,46.58,0,0,1,44.72-65L90.43,36.54A34.38,34.38,0,0,0,57.36,79.75C57.67,80.88,58,82,58.43,83l13.66-8.19L68,63.93l12.9-13.25,16.31-3.51L101.9,53l-7.52,7.61-6.55,2.06-4.69,4.82,2.3,6.38s4.64,4.94,4.65,4.94l6.57-1.74,4.67-5.13,10.2-3.24,3,6.84L104.05,88.43,86.41,94l-7.92-8.81L64.7,93.48a34.44,34.44,0,0,0,28.72,11.59L96.61,117A46.6,46.6,0,0,1,54.13,99.83l-10.64,6.38a58.81,58.81,0,0,0,99.6-9.77l11.8,4.29A70.77,70.77,0,0,1,108.92,139.3Z"></path></svg>
<span style="display: inline-block; width: 4px;"></span>
Modrinth
</a>
<span style="display: inline-block; width: 1em"></span>
<a href="https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights" style="display: inline-flex; align-items: center">
<svg aria-hidden="true" viewBox="-2017 853 43 23" data-view-component="true" src="https://www.curseforge.com/Content/2-0-8083-18015/Skins/CurseForge/images/anvil.svg" width="42" height="42" style="fill: var(--ls_theme_primary)"><path fill-rule="evenodd" d="M-2005.7,853l0.7,3c-3.5,0-12,0-12,0s0.2,0.9,0.3,1c0.3,0.5,0.6,1.1,1,1.5c1.9,2.2,5.2,3.1,7.9,3.6  c1.9,0.4,3.8,0.5,5.7,0.6l2.2,5.9h1.2l0.7,1.9h-1l-1.7,5.5h16.7l-1.7-5.5h-1l0.7-1.9h1.2c0,0,1-6.1,4.1-8.9c3-2.8,6.7-3.2,6.7-3.2  V853H-2005.7z M-1988.9,868.1c-0.8,0.5-1.7,0.5-2.3,0.9c-0.4,0.2-0.6,0.8-0.6,0.8c-0.4-0.9-0.9-1.2-1.5-1.4  c-0.6-0.2-1.7-0.1-3.2-1.4c-1-0.9-1.1-2.1-1-2.7v-0.1c0-0.1,0-0.1,0-0.2s0-0.2,0.1-0.3l0,0l0,0c0.2-0.6,0.7-1.2,1.7-1.6  c0,0-0.7,1,0,2c0.4,0.6,1.2,0.9,1.9,0.5c0.3-0.2,0.5-0.6,0.6-0.9c0.2-0.7,0.2-1.4-0.4-1.9c-0.9-0.8-1.1-1.9-0.5-2.6  c0,0,0.2,0.9,1.1,0.8c0.6,0,0.6-0.2,0.4-0.4c-0.1-0.3-1.4-2.2,0.5-3.6c0,0,1.2-0.8,2.6-0.7c-0.8,0.1-1.7,0.6-2,1.4c0,0,0,0,0,0.1  c-0.3,0.8-0.1,1.7,0.5,2.5c0.4,0.6,0.9,1.1,1.1,1.9c-0.3-0.1-0.5,0-0.7,0.2c-0.2,0.2-0.3,0.6-0.2,0.9c0.1,0.2,0.3,0.4,0.5,0.4  c0.1,0,0.1,0,0.2,0h0.1c0.3-0.1,0.5-0.5,0.4-0.8c0.2,0.2,0.3,0.7,0.2,1c0,0.3-0.2,0.6-0.3,0.8c-0.1,0.2-0.3,0.4-0.4,0.6  s-0.2,0.4-0.2,0.6c0,0.2,0,0.5,0.1,0.7c0.4,0.6,1.2,0,1.4-0.5c0.3-0.6,0.2-1.3-0.2-1.9c0,0,0.7,0.4,1.2,1.8  C-1987.4,866.2-1988.1,867.6-1988.9,868.1z"></path></svg>
<span style="display: inline-block; width: 4px;"></span>
CurseForge
</a>
<span style="display: inline-block; width: 1em"></span>
<a href="https://github.com/LambdAurora/LambDynamicLights/releases" style="display: inline-flex; align-items: center">
<svg aria-hidden="true" viewBox="0 0 16 16" data-view-component="true" width="42" height="42" style="fill: var(--ls_theme_primary)"><path fill-rule="evenodd" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path></svg>
<span style="display: inline-block; width: 4px;"></span>
GitHub
</a>
</div>
<!-- modrinth_exclude.long_end -->

# 📖 Compatibility

- [Sodium] is recommended for better performances.
- [Canvas] is compatible but still WIP: expect huge lag spike with it until a proper lighting API is done in Canvas.
- **OptiFabric is obviously incompatible.**

[fabric]: https://fabricmc.net
[Mod loader: Fabric]: https://img.shields.io/badge/modloader-Fabric-1976d2?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFHGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDIgNzkuMTYwOTI0LCAyMDE3LzA3LzEzLTAxOjA2OjM5ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDE4LTEyLTE2VDE2OjU0OjE3LTA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgZGM6Zm9ybWF0PSJpbWFnZS9wbmciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHBob3Rvc2hvcDpJQ0NQcm9maWxlPSJzUkdCIElFQzYxOTY2LTIuMSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIj4gPHhtcE1NOkhpc3Rvcnk+IDxyZGY6U2VxPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY3JlYXRlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHN0RXZ0OndoZW49IjIwMTgtMTItMTZUMTY6NTQ6MTctMDg6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE4IChXaW5kb3dzKSIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4/HiGMAAAAtUlEQVRYw+XXrQqAMBQF4D2P2eBL+QIG8RnEJFaNBjEum+0+zMQLtwwv+wV3ZzhhMDgfJ0wUSinxZUQWgKos1JP/AbD4OneIDyQPwCFniA+EJ4CaXm4TxAXCC0BNHgLhAdAnx9hC8PwGSRtAFVMQjF7cNTWED8B1cgwW20yfJgAvrssAsZ1cB3g/xckAxr6FmCDU5N6f488BrpCQ4rQBJkiMYh4ACmLzwOQF0CExinkCsvw7vgGikl+OotaKRwAAAABJRU5ErkJggg==
[Fabric API]: https://www.curseforge.com/minecraft/mc-mods/fabric-api "Fabric API CurseForge page"
[ModMenu]: https://modrinth.com/mod/modmenu
[Sodium]: https://www.curseforge.com/minecraft/mc-mods/sodium "Sodium CurseForge page"
[Canvas]: https://www.curseforge.com/minecraft/mc-mods/canvas-renderer "Canvas CurseForge page"
[LambdAurora Discord]: https://discord.lambdaurora.dev
