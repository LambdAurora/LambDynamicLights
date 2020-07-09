# LambDynamicLights API

You added a new entity that could emit light? You want that it dynamically emits light?

Then try the API of this mod!

## Quick note

Every time entity is referenced it means either an entity or a block entity.

Block entity dynamic lighting is non-recommended if avoidable with block states.

If your entity re-implements tick without calling the super method the dynamic light handler will not work.

## Dynamic light handlers

### `DynamicLightHandler`

A dynamic light handler is an interface with one method: `int getLuminance(T lightSource)`.

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
registerDynamicLightHandler(EntityType.BLAZE, entity -> 10);
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

 - `DynamicLightHandler#makeLivingEntityHandler` will merge the given handler with a basic handler for living entity which detects item light sources.
 - `DynamicLightHandler#makeCreeperEntityHandler` will optionally merge the given handler with a basic handler for creepers. May be useful for Creepers mod.
 - `LambDynLights#getLuminanceFromItemStack` will return the luminance value of the given item stack.
 
## `#lambdynlights:water_sensitive` tag

This is an item tag which lists water-sensitive light-emitting items.

Every items listed in this tag will not emit light in water.
