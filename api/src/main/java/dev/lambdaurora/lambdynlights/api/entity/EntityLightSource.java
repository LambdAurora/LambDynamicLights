/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.api.predicate.LightSourceLocationPredicate;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents an entity light source.
 *
 * @param predicate the predicate to select which entities emit the given luminance
 * @param luminances the luminance sources
 * @author LambdAurora
 * @version 4.2.0
 * @since 4.0.0
 */
public record EntityLightSource(EntityPredicate predicate, List<EntityLuminance> luminances) {
	public static final Codec<EntityLightSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							EntityPredicate.CODEC.fieldOf("match").forGetter(EntityLightSource::predicate),
							EntityLuminance.LIST_CODEC.fieldOf("luminance").forGetter(EntityLightSource::luminances)
					)
					.apply(instance, EntityLightSource::new)
	);

	/**
	 * Gets the luminance of the entity.
	 *
	 * @param itemLightSourceManager the item light source manager
	 * @param entity the entity
	 * @return the luminance value between {@code 0} and {@code 15}
	 */
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (this.predicate.test(entity)) {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.luminances);
		}

		return 0;
	}

	/**
	 * Represents a predicate to match entities with.
	 * <p>
	 * This is inspired from the {@linkplain net.minecraft.advancements.criterion.EntityPredicate entity predicate}
	 * found in advancements but with fewer features since this one needs to work on the client.
	 *
	 * @param entityType the entity type predicate to match if present
	 * @param located the location predicate to match if present
	 * @param effects the effects predicate to match if present
	 * @param flags the entity flags predicate to match if present
	 * @param equipment the equipment predicate to match if present
	 * @param vehicle the vehicle predicate to match if present
	 * @param passenger the passenger predicate to match if present
	 * @param slots the slots predicate to match if present
	 */
	public record EntityPredicate(
			Optional<EntityTypePredicate> entityType,
			Optional<LightSourceLocationPredicate> located,
			Optional<MobEffectsPredicate> effects,
			Optional<EntityFlagsPredicate> flags,
			Optional<EntityEquipmentPredicate> equipment,
			Optional<EntityPredicate> vehicle,
			Optional<EntityPredicate> passenger,
			Optional<SlotsPredicate> slots,
			Optional<DataComponentExactPredicate> components
	) {
		public static final Codec<EntityPredicate> CODEC = Codec.recursive(
				"EntityPredicate",
				codec -> RecordCodecBuilder.create(
						instance -> instance.group(
										EntityTypePredicate.CODEC.optionalFieldOf("type")
												.forGetter(EntityPredicate::entityType),
										LightSourceLocationPredicate.CODEC.optionalFieldOf("location")
												.forGetter(EntityPredicate::located),
										MobEffectsPredicate.CODEC.optionalFieldOf("effects")
												.forGetter(EntityPredicate::effects),
										EntityFlagsPredicate.CODEC.optionalFieldOf("flags")
												.forGetter(EntityPredicate::flags),
										EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment")
												.forGetter(EntityPredicate::equipment),
										codec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle),
										codec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger),
										SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots),
										DataComponentExactPredicate.CODEC.optionalFieldOf("components")
												.forGetter(EntityPredicate::components)
								)
								.apply(instance, EntityPredicate::new)
				)
		);

		/**
		 * Tests the predicate with the given entity.
		 *
		 * @param entity the entity to test
		 * @return {@code true} if the entity matches this predicate, or {@code false} otherwise
		 */
		public boolean test(@Nullable Entity entity) {
			if (entity == null) {
				return false;
			} else if (this.entityType.isPresent() && !this.entityType.get().matches(entity.getType())) {
				return false;
			} else if (this.located.isPresent() && !this.located.get().matches(entity.level(), entity.getX(), entity.getY(), entity.getZ())) {
				return false;
			} else if (this.effects.isPresent() && !this.effects.get().matches(entity)) {
				return false;
			} else if (this.flags.isPresent() && !this.flags.get().matches(entity)) {
				return false;
			} else if (this.equipment.isPresent() && !this.equipment.get().matches(entity)) {
				return false;
			} else if (this.vehicle.isPresent() && !this.vehicle.get().test(entity.getVehicle())) {
				return false;
			} else if (this.passenger.isPresent()
					&& entity.getPassengers().stream().noneMatch(passenger -> this.passenger.get().test(passenger))) {
				return false;
			} else if (this.slots.isPresent() && !((SlotsPredicate) this.slots.get()).matches(entity)) {
				return false;
			} else {
				return this.components.isEmpty() || !this.components.get().test(entity);
			}
		}

		/**
		 * Creates a new builder instance.
		 *
		 * @return The builder instance.
		 * @since 4.1.0
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * Represents a builder for creating new {@link EntityLightSource} instances.
		 *
		 * @since 4.1.0
		 */
		public static class Builder {
			private Optional<EntityTypePredicate> entityType = Optional.empty();
			private Optional<LightSourceLocationPredicate> located = Optional.empty();
			private Optional<MobEffectsPredicate> effects = Optional.empty();
			private Optional<EntityFlagsPredicate> flags = Optional.empty();
			private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
			private Optional<EntityPredicate> vehicle = Optional.empty();
			private Optional<EntityPredicate> passenger = Optional.empty();
			private Optional<SlotsPredicate> slots = Optional.empty();
			private Optional<DataComponentExactPredicate> components = Optional.empty();

			public Builder of(HolderGetter<EntityType<?>> holderGetter, EntityType<?> type) {
				this.entityType = Optional.of(EntityTypePredicate.of(holderGetter, type));
				return this;
			}

			@SuppressWarnings("deprecation")
			public Builder of(HolderGetter<EntityType<?>> holderGetter, EntityType<?>... types) {
				// Follow the pattern set by Vanilla's EntityTypePredicate.of,
				// which does not seem to use holderGetter for direct sets.
				this.entityType = Optional.of(new EntityTypePredicate(HolderSet.direct(EntityType::builtInRegistryHolder, types)));
				return this;
			}

			public Builder of(HolderGetter<EntityType<?>> holderGetter, TagKey<EntityType<?>> tag) {
				this.entityType = Optional.of(EntityTypePredicate.of(holderGetter, tag));
				return this;
			}

			/**
			 * Sets the entity type predicate to match with.
			 *
			 * @param entityTypePredicate the entity type predicate to match if present
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder entityType(EntityTypePredicate entityTypePredicate) {
				this.entityType = Optional.of(entityTypePredicate);
				return this;
			}

			/**
			 * Sets the location predicate to match with.
			 *
			 * @param builder the location predicate builder
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder located(LightSourceLocationPredicate.Builder builder) {
				this.located = Optional.of(builder.build());
				return this;
			}

			/**
			 * Sets the effects predicate to match with.
			 *
			 * @param builder the effects predicate builder
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder effects(MobEffectsPredicate.Builder builder) {
				this.effects = builder.build();
				return this;
			}

			/**
			 * Sets the entity flags predicate to match with.
			 *
			 * @param builder the entity flags predicate builder
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder flags(EntityFlagsPredicate.Builder builder) {
				this.flags = Optional.of(builder.build());
				return this;
			}

			/**
			 * Sets the equipment predicate to match with.
			 *
			 * @param builder the equipment predicate builder
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder equipment(EntityEquipmentPredicate.Builder builder) {
				this.equipment = Optional.of(builder.build());
				return this;
			}

			/**
			 * Sets the equipment predicate to match with.
			 *
			 * @param equipmentPredicate the equipment predicate to match
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder equipment(EntityEquipmentPredicate equipmentPredicate) {
				this.equipment = Optional.of(equipmentPredicate);
				return this;
			}

			/**
			 * Sets the vehicle entity predicate to match with.
			 *
			 * @param builder the vehicle entity predicate builder
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder vehicle(Builder builder) {
				this.vehicle = Optional.of(builder.build());
				return this;
			}

			/**
			 * Sets the passenger entity predicate to match with.
			 *
			 * @param builder the passenger entity predicate builder
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder passenger(Builder builder) {
				this.passenger = Optional.of(builder.build());
				return this;
			}

			/**
			 * Sets the slots predicate to match with.
			 *
			 * @param slotsPredicate the slots predicate to match if present
			 * @return {@code this}
			 */
			@Contract("_ -> this")
			public Builder slots(SlotsPredicate slotsPredicate) {
				this.slots = Optional.of(slotsPredicate);
				return this;
			}

			/**
			 * Sets the exact data components predicate to match with.
			 *
			 * @param builder the exact data components predicate builder
			 * @return {@code this}
			 * @since 4.2.0
			 */
			@Contract("_ -> this")
			public Builder components(DataComponentExactPredicate.Builder builder) {
				return this.components(builder.build());
			}

			/**
			 * Sets the exact data components predicate to match with.
			 *
			 * @param componentsPredicate the exact data components predicate to match if present
			 * @return {@code this}
			 * @since 4.2.0
			 */
			@Contract("_ -> this")
			public Builder components(DataComponentExactPredicate componentsPredicate) {
				this.components = Optional.of(componentsPredicate);
				return this;
			}

			/**
			 * Builds the resulting {@link EntityPredicate}.
			 *
			 * @return the resulting {@link EntityPredicate}
			 */
			public EntityPredicate build() {
				return new EntityPredicate(
						this.entityType,
						this.located,
						this.effects,
						this.flags,
						this.equipment,
						this.vehicle,
						this.passenger,
						this.slots,
						this.components
				);
			}
		}
	}
}
