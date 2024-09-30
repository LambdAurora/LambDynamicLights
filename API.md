# LambDynamicLights API

You added a new entity that could emit light? You want that it dynamically emits light?

Then try the API of this mod!

## Quick note

Every time an entity is referenced it means either an entity or a block entity.

Block entity dynamic lighting is non-recommended if avoidable with block states.

If your entity re-implements tick without calling the super method the dynamic light handler will not work.

## LambDynamicLights entrypoint

Any API calls should be done in the custom entrypoint.

To use the entrypoint, make a new class implementing `DynamicLightsInitializer`,
add in your `fabric.mod.json` this:
```json
  "entrypoints": {
    "dynamiclights": [
      "path.to.your.Class"
    ]
  }
```

Once done, you can call the methods presented in the rest of this document in the method `onInitializeDynamicLights`.

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

By default, every item will emit the same amount of light as their assigned block if possible.

But for items that are not assigned to a block, or for items that should not light up underwater, there's JSON files to write!

The JSONs are located in `<namespace>:dynamiclights/item/<file>.json`.

### JSON item light source

The format is simple:

- `match` - The [item predicate](https://minecraft.wiki/w/Template:Nbt_inherit/conditions/item/template) to match the affected items.
- `luminance` - Can either be:
  - a number between `0` and `15`, which corresponds to the luminance value.
  - an object with a type and arguments, which can be:
    - `{ "type": "block", "block": "<block identifier>" }` to copy the luminance value of the specified block.
    - `{ "type": "block_self" }` to copy the luminance value of the block already associated with the matched item.
- `water_sensitive` *(Optional)* - `true` if the item doesn't emit light when submerged in water, or `false` otherwise.
- `silence_error` *(Optional)* - `true` to silence any kind of runtime error from the item light source,
  this is heavily discouraged unless you know what you're doing as you will not be made aware of errors!  
  Errors will still be logged if in a development environment or if the `lambdynamiclights.resource.force_log_errors` property is set to `true`.

#### Examples

##### `lambdynlights:dynamiclights/item/fire_charge.json`

```json
{
	"match": {
		"items": "minecraft:fire_charge"
	},
	"luminance": 10,
	"water_sensitive": true
}
```

##### `lambdynlights:dynamiclights/item/lava_bucket.json`

```json
{
	"match": {
		"items": "minecraft:lava_bucket"
	},
	"luminance": {
		"type": "block",
		"block": "minecraft:lava"
	},
	"water_sensitive": true
}
```

##### `lambdynlights:dynamiclights/item/nether_star.json`

```json
{
	"match": {
		"items": "minecraft:nether_star"
	},
	"luminance": 8
}
```

##### `lambdynlights:dynamiclights/item/torch.json`

```json
{
	"match": {
		"items": [
			"minecraft:torch",
			"minecraft:redstone_torch",
			"minecraft:soul_torch"
		]
	},
	"luminance": {
		"type": "block_self"
	},
	"water_sensitive": true
}
```
