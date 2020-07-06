# LambDynamicLights changelog

## v1.0.0

 - Initial release.
 - Added dynamic lighting to the game.
 
## v1.1.0

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
