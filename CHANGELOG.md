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

## 2.3.1
- Updated to Minecraft 1.20 ([#156](https://github.com/LambdAurora/LambDynamicLights/pull/156))
- Updated [SpruceUI].

[SpruceUI]: https://github.com/LambdAurora/SpruceUI "SpruceUI page"
[pridelib]: https://github.com/Queerbric/pridelib "Pridelib page"
[Sodium]: https://modrinth.com/mod/sodium "Sodium Modrinth page"
[Canvas Renderer]: https://www.curseforge.com/minecraft/mc-mods/canvas-renderer "Canvas Renderer CurseForge page"
