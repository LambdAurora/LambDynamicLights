# LambDynamicLights changelog

## 1.0.0

- Initial release.
- Added dynamic lighting to the game.
 
## 1.1.0

- Added item frame dynamic lighting.
- Added API.
- Added more options.
  - Added entities dynamic lighting toggle.
  - Added block entities dynamic lighting toggle.
- Added block entity dynamic lighting.
  - Allow dynamic lighting for block entities without block states.
  - Should not be used in cases where block states can be used.
- Added compatibility with [Lil Tater Reloaded](https://github.com/Yoghurt4C/LilTaterReloaded).
  - Lil taters will light up the area if they hold an item emitting light.
- Fixed light not moving when light source was moving too slow.
- Fixed lighting errors with Sodium.

## 1.2.0

- Added water-sensitive check for items and light sources.
  - Added data item tag `#lambdynlights:water_sensitive` which lists every item which can't light up in the water.
  - Added an option to enable/disable the feature
- Updated [SpruceUI] to 1.5.6 to fix latest snapshots issues.
- Added "early/WIP" compatibility with [Canvas Renderer].
  - Added a warning message about performance issues. 
- Fixed a crash with [Sodium] rc7 with smooth lighting set to HIGH.
 
### 1.2.1

- Added TNT dynamic lighting.
- Added lighting options for TNT and Creepers.
- Added luminance value to Fire charge item.
- Updated [SpruceUI] to 1.5.8
- Fixed player dynamic lighting not getting tracked when changing dimensions.

### 1.2.2

- Changed video options dynamic lighting button to redirect to LambDynamicLights settings.
- Fixed random crash.

## 1.3.0

- Added Simplified Chinese translations.
- Added German translations.
- Added Mexican Spanish translations ([#13](https://github.com/LambdAurora/LambDynamicLights/pull/13)).
- Added Polish translations ([#15](https://github.com/LambdAurora/LambDynamicLights/pull/15)).
- Added Russian translations ([#9](https://github.com/LambdAurora/LambDynamicLights/pull/9)).
- Added spectral arrow as item emitting light ([#17](https://github.com/LambdAurora/LambDynamicLights/pull/17)).
- Added dynamic lighting on glowing entities ([#17](https://github.com/LambdAurora/LambDynamicLights/pull/17)).
- Updated to Minecraft 1.16.2
- Updated [SpruceUI] to 1.6.2.
- Fixed dynamic lighting update issues at chunk borders ([#12](https://github.com/LambdAurora/LambDynamicLights/issues/12)).
- Fixed water-sensitive items lighting up in water on dedicated servers. ([#3](https://github.com/LambdAurora/LambDynamicLights/issues/3))
  - Added new JSON API to add item luminance and water-sensitivity through resource packs.
  - Added `DynamicLightHandler#isWaterSensitive` to make some entities water-sensitive like the blaze.
- Fixed incompatibility with future [Sodium] versions. ([#6](https://github.com/LambdAurora/LambDynamicLights/issues/6))

### 1.3.1

- Fixed entity lighting issue with [Sodium] 0.1.0. ([#23](https://github.com/LambdAurora/LambDynamicLights/issues/23))

### 1.3.2

- Added entity lighting capabilities to minecarts.
- Added `DynamicLightsInitializer` and `dynamiclights` entrypoint.
- Added Brazilian Portuguese translations.
- Added Estonian translations.
- Added Hindi translations.
- Added Turkish translations.
- Updated French translations.
- Fixed memory leak in dynamic light source tracking. ([#30](https://github.com/LambdAurora/LambDynamicLights/issues/30))

### 1.3.3

- Added Italian translations ([#40](https://github.com/LambdAurora/LambDynamicLights/pull/40)).
- Optimized dynamic lighting update methods.
- Fixed crash when leaving world with some minimaps mods. ([#37](https://github.com/LambdAurora/LambDynamicLights/issues/37), [#41](https://github.com/LambdAurora/LambDynamicLights/issues/41))
- Fixed crash with Immersive Portals ([#39](https://github.com/LambdAurora/LambDynamicLights/issues/39)).
- Updated [SpruceUI], and Fabric API dependencies.

### 1.3.4

- Fix ghost dynamic light source ([#47](https://github.com/LambdAurora/LambDynamicLights/issues/47)).
- Fix missing background in settings screen.
- Updated [SpruceUI].

## 2.0.0

- Updated to Minecraft 1.17.
- Updated to Java 16.
- Added dynamic lighting to the Glow Squid.
- And more.

### 2.0.1

- Fixed mixin warnings ([#65](https://github.com/LambdAurora/LambDynamicLights/issues/65)).
- Removed any usage of a library.
- Updated [SpruceUI].

### 2.0.2

- Added Swedish translations ([#68](https://github.com/LambdAurora/LambDynamicLights/pull/68)).
- Block items now respect the `BlockStateTag` in item's NBT ([#71](https://github.com/LambdAurora/LambDynamicLights/issues/71)).
- Fixed glow item frames not emitting light ([#63](https://github.com/LambdAurora/LambDynamicLights/issues/63)).
- Fixed minecart not emitting light when holding a light source ([#70](https://github.com/LambdAurora/LambDynamicLights/issues/70)).
- Fixed glow berries not emitting light ([#72](https://github.com/LambdAurora/LambDynamicLights/issues/72)).
- Fixed glow squids not dimming when attacked ([#75](https://github.com/LambdAurora/LambDynamicLights/issues/75)).
- Fixed LambDynamicLights not rebuilding the correct chunks when at Y -18 or Y -50 ([#76](https://github.com/LambdAurora/LambDynamicLights/issues/76)).

## 2.1.0

- Added individual entity toggles ([#64](https://github.com/LambdAurora/LambDynamicLights/issues/64), [#79](https://github.com/LambdAurora/LambDynamicLights/issues/79)).
- Optimized hot methods `maxDynamicLightLevel` and `updateTracking` ([#84](https://github.com/LambdAurora/LambDynamicLights/pull/84)).
- Use `HashSet` for `dynamicLightSources` ([#85](https://github.com/LambdAurora/LambDynamicLights/pull/85)).
- Cache config values for better performances ([#80](https://github.com/LambdAurora/LambDynamicLights/issues/80)).

### 2.1.1

- Updated to Minecraft 1.19.
- Updated [SpruceUI].
- Updated [pridelib].

### 2.1.2

- Fixed dependencies declaration.

## 2.2.0

- Added option to disable self dynamic lighting.
- Updated Russian translations ([#116](https://github.com/LambdAurora/LambDynamicLights/pull/116), [#121](https://github.com/LambdAurora/LambDynamicLights/pull/121)).
- Added Ukrainian translations ([#120](https://github.com/LambdAurora/LambDynamicLights/pull/120)).

## 2.3.0

- Added Traditional Chinese translations ([#142](https://github.com/LambdAurora/LambDynamicLights/pull/142)).
- Updated Brazilian Portuguese translations ([#135](https://github.com/LambdAurora/LambDynamicLights/pull/135)).
- Updated Simplified Chinese translations ([#133](https://github.com/LambdAurora/LambDynamicLights/pull/133)).
- Updated to Minecraft 1.19.4 ([#144](https://github.com/LambdAurora/LambDynamicLights/pull/144)).
- Updated [SpruceUI].
- Updated [pridelib].

### 2.3.1

- Fixed ATLauncher configuration somehow ([#152](https://github.com/LambdAurora/LambDynamicLights/pull/152)).
- Updated to Minecraft 1.20 ([#156](https://github.com/LambdAurora/LambDynamicLights/pull/156))
- Updated [SpruceUI].
- Updated night config.

### 2.3.2

- Fixed Fabric API (Indigo Renderer) compatibility
  (by [#182](https://github.com/LambdAurora/LambDynamicLights/pull/182),
  issue [#172](https://github.com/LambdAurora/LambDynamicLights/issues/172)).
- Fixed Sodium compatibility
  (by [#178](https://github.com/LambdAurora/LambDynamicLights/pull/178), 
  issue [#175](https://github.com/LambdAurora/LambDynamicLights/issues/175)).
- Fixed Minecraft version restriction.
- Cleaned up some item light source code.

### 2.3.3

- Added Japanese translations ([#187](https://github.com/LambdAurora/LambDynamicLights/pull/187)).
- Updated to Minecraft 1.20.2 ([#194](https://github.com/LambdAurora/LambDynamicLights/pull/194)).
  - Fixed settings screen crash ([#186](https://github.com/LambdAurora/LambDynamicLights/issues/186)).
- Fixed usage of the Fabric mod identifier ([#188](https://github.com/LambdAurora/LambDynamicLights/issues/188)).

### 2.3.4

- Added Indonesian translations ([#203](https://github.com/LambdAurora/LambDynamicLights/pull/203)).
- Updated Ukrainian translations ([#195](https://github.com/LambdAurora/LambDynamicLights/pull/195)).
- Updated to Minecraft 1.20.4 ([#202](https://github.com/LambdAurora/LambDynamicLights/pull/202)).

## 3.0.0

- Changed how item light sources are defined in resource packs:
  - Now item light sources support a wide-range of selection predicates thanks to data-driven improvements in the base game.
    - This means enchanted items can now selectively light up, this should mostly address ([#89](https://github.com/LambdAurora/LambDynamicLights/issues/89)).
  - Please refer yourself to the documentation for more details.
- Updated to Minecraft 1.21 ([#227](https://github.com/LambdAurora/LambDynamicLights/pull/227)).
- Updated configuration library.
  - Configuration corruption should now be fixed.
- Updated Mexican Spanish translations ([#214](https://github.com/LambdAurora/LambDynamicLights/pull/214)).
- Updated Italian translations ([#232](https://github.com/LambdAurora/LambDynamicLights/pull/232)).
- Updated Polish translations ([#235](https://github.com/LambdAurora/LambDynamicLights/pull/235)).
- Removed block entity lighting as the use-case was extremely niche.
  - This may be re-introduced if a valid use-case is found.
- Switched license to [Lambda License](https://github.com/LambdAurora/LambDynamicLights/blob/bbefb8860bca2e797f8a2ba8a59d1120b6e1c7b4/LICENSE).

### 3.0.1

- Fixed crash due to Mixin plugin ([#239](https://github.com/LambdAurora/LambDynamicLights/issues/239)).

## 3.1.0

- Improved general performances, especially in worst-case scenarios.
- Added support for falling block entities ([#93](https://github.com/LambdAurora/LambDynamicLights/issues/93)).
- Added settings access in Sodium.
- Updated Simplified Chinese translations ([#242](https://github.com/LambdAurora/LambDynamicLights/pull/242)).

### 3.1.1

- Fixed in-world item light source data reload not applying.

### 3.1.2

- Added base light of 8 to allays.
- Fixed dynamic lighting of various projectiles.
- Fixed water-sensitive items lighting up underwater while they shouldn't.

### 3.1.3

- Fixed item frames and other "block-attached" entities not ticking properly on the integrated server.

### 3.1.4

- Added support for Trinkets and Accessories.

## 3.2.0

- Updated to Minecraft 1.21.2.
- Added base light of 8 to allays.
- Improved settings GUI, especially the entity dynamic light sources list.
- Tweaked the new dynamic lighting engine introduced in 3.1.0.
- Updated Estonian translations ([#243](https://github.com/LambdAurora/LambDynamicLights/pull/243)).
- Updated [SpruceUI].

### 3.2.1

- Same changes as v3.1.1 but for 1.21.3.
- Fixed in-world item light source data reload not applying.

### 3.2.2

- Same changes as v3.1.2 but for 1.21.3.
- Fixed dynamic lighting of various projectiles.
- Fixed water-sensitive items lighting up underwater while they shouldn't.

### 3.2.3

- Same changes as v3.1.3 but for 1.21.3.
- Fixed item frames and other "block-attached" entities not ticking properly on the integrated server.

### 3.2.4

- Same changes as v3.1.4 but for 1.21.3.
- Added support for Trinkets and Accessories.

## 4.0.0

- Added the ability to define entity light sources in resource packs.
  - Please refer yourself to the documentation for more details.
- Added display entities dynamic lighting ([#209](https://github.com/LambdAurora/LambDynamicLights/issues/209)).
  - This affects both block and item displays.
  - If a custom brightness is defined then dynamic lighting disables itself.
- Added a new API to define fully custom dynamic lighting of varying shapes.
  - Added dynamic lighting to Beacon beams ([#115]).
  - Added dynamic lighting to End Gateway beams ([#115]).
  - Added dynamic lighting to Guardian lasers ([#115]).
- Added debug settings and renderers to facilitate debugging.
  - Added a debug renderer of active dynamic lighting cells.
  - Added a debug renderer to display chunk rebuilds.
  - Added a debug renderer of dynamic light levels.
  - Added a debug renderer of display the bounding boxes of custom dynamic light sources.
- Updated the data displayed in the F3 HUD to show more information about dynamic lighting.
- Refactored heavily the ticking of entity dynamic lighting to make it more abstract.
- Refactored heavily how chunk rebuilds are queued and how dynamic light sources are represented.
- Fixed API publication for loom-based setups.
- Added Upside-down English translations ([#254](https://github.com/LambdAurora/LambDynamicLights/pull/254)).
- Updated Dutch translations ([#252](https://github.com/LambdAurora/LambDynamicLights/pull/252)).
- Updated German translations ([#253](https://github.com/LambdAurora/LambDynamicLights/pull/253)).
- Updated Italian translations ([#255](https://github.com/LambdAurora/LambDynamicLights/pull/255)).
- Updated [SpruceUI].

### 4.0.1

- Added Malay and Malay (Jawi) translations ([#256](https://github.com/LambdAurora/LambDynamicLights/pull/256)).
- Fixed Upside-down English translations ([#257](https://github.com/LambdAurora/LambDynamicLights/pull/257)).
- Fixed custom dynamic light sources sometimes not updating previously lit chunks.

### 4.0.2

- Improved loading/saving of the configuration file, this should significantly reduce corruption issues.
  - If the file still somehow corrupts, now the file is backed up and a default configuration is loaded instead.
- Added support for Curios. This is only relevant with Sinytra Connector or forks.
- Fixed entity dynamic light sources not being saved and loaded at/from the right path in the configuration.
- Fixed throwable item projectiles with the `Item` property set not emitting light if the item emits light ([#265](https://github.com/LambdAurora/LambDynamicLights/issues/265)).
- Updated Traditional Chinese translations ([#261](https://github.com/LambdAurora/LambDynamicLights/pull/261)).
- Updated Turkish translations ([#263](https://github.com/LambdAurora/LambDynamicLights/pull/263)).
- Updated Simplified Chinese translations ([#264](https://github.com/LambdAurora/LambDynamicLights/pull/264)).
- Updated [SpruceUI].

## 4.1.0

- Added datagen and helper methods to API ([#267](https://github.com/LambdAurora/LambDynamicLights/pull/267)).
  - This should allow any modders who wishes to datagen LambDynamicLights item or entity light source JSON files to do so easily.
- Moved `lambdynlights:glow_squid`, `lambdynlights:magma_cube`, `lambdynlights:water_sensitive`, and `lambdynlights:wet_sensitive` to the API artifact.
  - The water sensitivity setting doesn't affect entity luminance anymore (items are still affected), however this still can be tweaked through resource packs.

### 4.1.1

- Backports some changes from the v4.2.0 versions.
- Fixed `/reload` crash ([#275](https://github.com/LambdAurora/LambDynamicLights/issues/275)).
- Added Spanish translations ([#268](https://github.com/LambdAurora/LambDynamicLights/pull/268)).
- Added missing Spanish Mexican translations from Spanish translations as a stop-gap.
- Updated German translations (thanks zOnlyKroks).
- Updated Russian translations ([#269](https://github.com/LambdAurora/LambDynamicLights/pull/269)).
- Updated Brazilian Portuguese translations ([#270](https://github.com/LambdAurora/LambDynamicLights/pull/270)).
- Updated Vietnamese translations ([#276](https://github.com/LambdAurora/LambDynamicLights/pull/276)).
- Updated [Yumi Commons] for better dependency resolution.

### 4.1.2

- Updated [Yumi Commons] to fix a crash on NeoForge with Sinytra Connector due to bad handling of JPMS.

### 4.1.3

- Backports some changes from the v4.2.7:
  - Added Argentine Spanish translations ([#279](https://github.com/LambdAurora/LambDynamicLights/pull/279)).
  - Updated Chinese translations ([#281](https://github.com/LambdAurora/LambDynamicLights/pull/281)).
  - Made tab in Sodium GUI translatable ([#282](https://github.com/LambdAurora/LambDynamicLights/pull/282)).

### 4.1.4

- Backports some changes from the v4.3.1:
  - Switched to [Yumi Minecraft Foundation Library] for entrypoint management and mod discovery.
    - This allows to use entrypoints more reliably on NeoForge and with full support of Sinytra Connector.
      Such entrypoints can be added through the `yumi:entrypoints` custom property.
    - This is a step towards better and official support of NeoForge.
  - Added debug information to crash reports.
  - Updated Turkish translations ([#285](https://github.com/LambdAurora/LambDynamicLights/pull/285)).
  - Updated Chinese translations ([#286](https://github.com/LambdAurora/LambDynamicLights/pull/286)).
  - Updated Italian translations ([#287](https://github.com/LambdAurora/LambDynamicLights/pull/287)).
  - Updated Polish translations ([#288](https://github.com/LambdAurora/LambDynamicLights/pull/288)).

### 4.1.5

- Backports some fixes from v4.3.2:
  - Fixed various crashes due to early Minecraft classes being loaded ([#289](https://github.com/LambdAurora/LambDynamicLights/issues/289)).
  - Updated [Yumi Minecraft Libraries: Foundation].

### 4.1.6

- Backports some changes from the v4.3.3:
  - Fixed some crashes related to debug renderers.

## 4.2.0

- Updated to Minecraft 1.21.5.
- Added dynamic lighting to Firefly particles.
- Added support for entity component predicates.
- Updated [SpruceUI].

### 4.2.1

- Fixed a crash on start due to a bad mixin remap.
- Fixed the throwable item projectiles entity dynamic light source JSON.

### 4.2.2

- Fixed French and French Canadian translations.
- Added Spanish translations ([#268](https://github.com/LambdAurora/LambDynamicLights/pull/268)).
- Added missing Spanish Mexican translations from Spanish translations as a stop-gap.
- Updated German translations (thanks zOnlyKroks).

### 4.2.3

- Hotfix performance issues due to a wrong Mixin injection.

### 4.2.4

- Updated Russian translations ([#269](https://github.com/LambdAurora/LambDynamicLights/pull/269)).
- Updated Brazilian Portuguese translations ([#270](https://github.com/LambdAurora/LambDynamicLights/pull/270)).

### 4.2.5

- Fixed `/reload` crash ([#275](https://github.com/LambdAurora/LambDynamicLights/issues/275)).
- Updated Vietnamese translations ([#276](https://github.com/LambdAurora/LambDynamicLights/pull/276)).
- Updated [Yumi Commons] for better dependency resolution.

### 4.2.6

- Updated [Yumi Commons] to fix a crash on NeoForge with Sinytra Connector due to bad handling of JPMS.

### 4.2.7

- Added Argentine Spanish translations ([#279](https://github.com/LambdAurora/LambDynamicLights/pull/279)).
- Updated Chinese translations ([#281](https://github.com/LambdAurora/LambDynamicLights/pull/281)).
- Made tab in Sodium GUI translatable ([#282](https://github.com/LambdAurora/LambDynamicLights/pull/282)).

### 4.2.8

- Backports some changes from the v4.3.1:
  - Switched to [Yumi Minecraft Foundation Library] for entrypoint management and mod discovery.
    - This allows to use entrypoints more reliably on NeoForge and with full support of Sinytra Connector.
      Such entrypoints can be added through the `yumi:entrypoints` custom property.
    - This is a step towards better and official support of NeoForge.
  - Updated Turkish translations ([#285](https://github.com/LambdAurora/LambDynamicLights/pull/285)).
  - Updated Chinese translations ([#286](https://github.com/LambdAurora/LambDynamicLights/pull/286)).
  - Updated Italian translations ([#287](https://github.com/LambdAurora/LambDynamicLights/pull/287)).
  - Updated Polish translations ([#288](https://github.com/LambdAurora/LambDynamicLights/pull/288)).

### 4.2.9

- Backports some fixes from v4.3.2:
  - Fixed various crashes due to early Minecraft classes being loaded ([#289](https://github.com/LambdAurora/LambDynamicLights/issues/289)).
  - Updated [Yumi Minecraft Libraries: Foundation].

### 4.2.10

- Backports some changes from the v4.3.3:
  - Fixed some crashes related to debug renderers.

## 4.3.0

- Updated to Minecraft 1.21.6.
- Updated [SpruceUI].
- Updated [pridelib].

### 4.3.1

- Switched to [Yumi Minecraft Libraries: Foundation] for entrypoint management and mod discovery.
  - This allows to use entrypoints more reliably on NeoForge and with full support of Sinytra Connector.
    Such entrypoints can be added through the `yumi:entrypoints` custom property.
  - This is a step towards better and official support of NeoForge.
- Updated Turkish translations ([#285](https://github.com/LambdAurora/LambDynamicLights/pull/285)).
- Updated Chinese translations ([#286](https://github.com/LambdAurora/LambDynamicLights/pull/286)).
- Updated Italian translations ([#287](https://github.com/LambdAurora/LambDynamicLights/pull/287)).
- Updated Polish translations ([#288](https://github.com/LambdAurora/LambDynamicLights/pull/288)).
- Updated [SpruceUI].

### 4.3.2

- Fixed various crashes due to early Minecraft classes being loaded ([#289](https://github.com/LambdAurora/LambDynamicLights/issues/289)).
- Updated [Yumi Minecraft Libraries: Foundation].

### 4.3.3

- Fixed some crashes related to debug renderers.

## 4.4.0

- Added Warden Sonic Boom attack particles dynamic lighting ([#307](https://github.com/LambdAurora/LambDynamicLights/issues/307)).
- Added option to control whether the Glowing effect gives dynamic lighting to entities ([#140](https://github.com/LambdAurora/LambDynamicLights/issues/140)).
- Added a keybind to toggle ON/OFF first-person dynamic lighting.
  - This may be useful to users who use shaders with first-person dynamic lighting included.
    With the addition of an additional mod, or Vanilla in 1.21.9,
    it is also possible to bind this to the same key which toggles shaders ([#248](https://github.com/LambdAurora/LambDynamicLights/issues/248)).
- Added support for Fabric resource conditions in dynamic light source files.
  - Fixed [#309](https://github.com/LambdAurora/LambDynamicLights/issues/309).
- Improved handling of capacity limit of the spatial lookup, making it resize dynamically ([#292](https://github.com/LambdAurora/LambDynamicLights/issues/292)).
  - If you had issues with too many light sources, this should at least fix the lack of light updates,
    however this still can affect performances.
- Fixed leak of the tracking of dynamic lighting of Firefly particles.
- Updated Ukrainian translations ([#304](https://github.com/LambdAurora/LambDynamicLights/pull/304)).
- Updated [Yumi Minecraft Libraries: Foundation].

[SpruceUI]: https://github.com/LambdAurora/SpruceUI "SpruceUI page"
[pridelib]: https://github.com/Queerbric/pridelib "Pridelib page"
[Yumi Commons]: https://github.com/YumiProject/yumi-commons "Yumi Commons page"
[Yumi Minecraft Libraries: Foundation]: https://github.com/YumiProject/yumi-minecraft-foundation-library "Yumi Minecraft Foundation Library page"
[Sodium]: https://modrinth.com/mod/sodium "Sodium Modrinth page"
[Canvas Renderer]: https://www.curseforge.com/minecraft/mc-mods/canvas-renderer "Canvas Renderer CurseForge page"
[#115]: https://github.com/LambdAurora/LambDynamicLights/issues/115
