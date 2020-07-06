# How does it work?

This is a little file explaining how the mod works and hopefully be as detailed as it can
to allow people to maintain the code over newer versions and remove the monopole OptiFine has.

## [UpcraftLP method](https://gist.github.com/UpcraftLP/93db478535cb3954cc9f3dc98bac91cf) & [AtomicStryker method](https://github.com/AtomicStryker/atomicstrykers-minecraft-mods/tree/1.14.3/DynamicLights)

Not used by this mod.

UpcraftLP method can be found here: https://gist.github.com/UpcraftLP/93db478535cb3954cc9f3dc98bac91cf

AtomicStryker method can be found here: https://github.com/AtomicStryker/atomicstrykers-minecraft-mods/tree/1.14.3/DynamicLights

Both methods suffer from a "laggy" dynamic lighting, the way the light moves is too much tied to
the block positions as it only injects at luminance getters in `WorldChunk` or a lighting provider.
It will only provide the dynamic light value of the light source if the block position is the position
of the light source.

It's better in a way as the F3 menu will actually report a light value instead of the lightmap coordinates
method, but will be significantly be less smooth in its transitions.

## Lightmap coordinates method

This method is used by this mod.

### Dynamic luminance injection

As [@Martmists](https://github.com/martmists) suggested, `WorldRenderer#getLightmapCoordinates` is a great entrypoint.

This method gives lightmap coordinates in the format `(skyLevel << 20 | blockLevel << 4)`
and to get the blockLevel from the lightmap use `LightmapTextureManager#getBlockLightCoordinates`.

The goal is to inject at TAIL into `getLightmapCoordinates` and get the vanilla value, then
get the dynamic value at the specified block position (which is calculated from all the dynamic
light sources, their distances and the luminance), if the dynamic value is higher than the block value
then we replace it.

This also means that we can't just do like the previous method and only give the source luminance but
we also have to calculate the surrounding luminance created by the dynamic light source in a specified
range (which is 7.75 to limit chunk rebuilding to 8 chunks (which is still a lot)).

When getting the dynamic light value at the specified position, it has to be a `double` and not
an integer as the light value calculated with the range has to be precise.
To modify the lightmap with the dynamic light value, it has to be multiplied by 16.0 instead of
using a bitshift to preserve as much as possible the precision.

Dynamic light value at a specified position is calculated in a for-loop with all the dynamic light
sources, only the highest light value is kept. The light value is calculated as follow:

```java
// dist being the distance between the light source origin and the position where the
// lightmap coordinates are requested.
double multiplier = 1.0 - dist / 7.75; // 7.75 because of the range
// luminace being the luminance of the dynamic light source.
double lightLevel = multiplier * (double) luminance;
```

`EntityRenderDispatcher#getLight` also needs an injection like `getLightmapCoordinates`, it needs
the same replacement of the lightmap value to avoid dark entities in a lit place.

### Chunk rebuilding

To apply the dynamic lighting we have to request the affected chunks a rebuild to rebuild the lightmap,
which is the most critic part performance-wise. Rebuilding a chunk cost performances,
that's why there's multiple mode: FASTEST, FAST and FANCY. The fast modes will limit the update.

To manage chunk rebuilding, we inject at `WorldRenderer#setupTerrain` (`WorldRenderer#render` at `WorldRenderer#setupTerrain` invoke if Sodium is present),
and if fast mode then we check when the last update was done.
If we can update, then we get the current dynamic light source position and get its chunk.
After that we get the surrounding affect chunks and build a new Set of chunk coordinates which
need to be updated.

## Shader based method

This would be more ideal performance-wise as it would give the smoothness of the second method
and avoid too many chunk rebuilding.

Note: the shader-based method will break in the case of any other renderer registered.
For compatibility it's the less ideal.
