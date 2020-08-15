# LambDynamicLights API

You added a new entity that could emit light? You want that it dynamically emits light?

Then try the API of this mod!

## Quick note

Every time entity is referenced it means either an entity or a block entity.

Block entity dynamic lighting is non-recommended if avoidable with block states.

If your entity re-implements tick without calling the super method the dynamic light handler will not work.

## Dynamic light handlers

### `DynamicLightHandler`

A dynamic light handler is an interface with different methods:
 - `int getLuminance(T lightSource)`
 - `boolean isWaterSensitive(T lightSource)` - with default `false`

The returned value is between 0 and 15 which are luminance values, `lightSource` is of the type of the entity and is the targeted entity.
The method is called for every entity matching the type at each tick.

### `DynamicLightHandlers`

That's where you register your handler!

Just call `DynamicLightHandlers#registerDynamicLightHandler(EntityType<T>, DynamicLightHandler<T>)`
or `DynamicLightHandlers#registerDynamicLightHandler(BlockEntityType<T>, DynamicLightHandler<T>)`
to register your handler!

If a handler is already registered for this entity, then it will merge the two handlers with a `Math#max` handler.

And that's all! The mod will light up your entities following your handler.

### Examples

#### Blaze

```java
registerDynamicLightHandler(EntityType.BLAZE, DynamicLightHandler.makeHandler(blaze -> 10, blaze -> true));
```

#### Enderman

```java
registerDynamicLightHandler(EntityType.ENDERMAN, entity -> {
    int luminance = 0;
    if (entity.getCarriedBlock() != null)
        luminance = entity.getCarriedBlock().getLuminance();
    return luminance;
});
```

#### Item frame

```java
registerDynamicLightHandler(EntityType.ITEM_FRAME, entity -> {
    World world = entity.getEntityWorld();
    return LambDynLights.getLuminanceFromItemStack(entity.getHeldItemStack(), !world.getFluidState(entity.getBlockPos()).isEmpty());
});
```

## Utility methods

 - `DynamicLightHandler#makeHandler` will transforms 2 functions into an handler.
 - `DynamicLightHandler#makeLivingEntityHandler` will merge the given handler with a basic handler for living entity which detects item light sources.
 - `DynamicLightHandler#makeCreeperEntityHandler` will optionally merge the given handler with a basic handler for creepers. May be useful for Creepers mod.
 - `LambDynLights#getLuminanceFromItemStack` will return the luminance value of the given item stack.
 
## Item light sources

By default every items will emit the same amount of light as their assigned block if possible.

But for items that are not assigned to a block, or for items that should not lit up underwater, there's JSON files to write!

The JSONs are located in `<modid>:dynamiclights/item/<file>.json`.

### JSON item light source

The format is simple:

- `item` - The identifier of the affected item.
- `luminance` - Either a number between `0` and `15` and corresponds to the luminance value, 
    or is a string with either a block identifier to get the luminance from 
    or `"block"` to use the default assigned block luminance value.
- `water_sensitive` *(Optional)* - `true` if the item doesn't emit light when submerged in water, else `false`.

#### Examples

##### `lambdynlights:dynamiclights/item/fire_charge.json`

```json
{
  "item": "minecraft:fire_charge",
  "luminance": 10,
  "water_sensitive": true
}
```

##### `lambdynlights:dynamiclights/item/lava_bucket.json`

```json
{
  "item": "minecraft:lava_bucket",
  "luminance": "minecraft:lava",
  "water_sensitive": true
}
```

##### `lambdynlights:dynamiclights/item/nether_star.json`

```json
{
  "item": "minecraft:nether_star",
  "luminance": 8
}
```

##### `lambdynlights:dynamiclights/item/torch.json`

```json
{
  "item": "minecraft:torch",
  "luminance": "block",
  "water_sensitive": true
}
```
