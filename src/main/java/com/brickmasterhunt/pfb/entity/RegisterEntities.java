package com.brickmasterhunt.pfb.entity;

import com.brickmasterhunt.pfb.pfb;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegisterEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE_DEFERRED_REGISTER =
            DeferredRegister.create(ForgeRegistries.ENTITIES, pfb.MOD_ID);

    public static final RegistryObject<EntityType<PhysicsFallingBlockEntity>> FALLING_BLOCK =
            ENTITY_TYPE_DEFERRED_REGISTER.register("falling_block", () -> EntityType.Builder.of(com.brickmasterhunt.pfb.entity.PhysicsFallingBlockEntity::new, MobCategory.MISC)
                    .sized(1.0F,1.0F)
                    .build(new ResourceLocation(pfb.MOD_ID, "falling_block").toString()));

    public static final RegistryObject<EntityType<PhysicsFallingBlockEntity>> FALLING_BLOCK_BUT_MINI_LOL =
            ENTITY_TYPE_DEFERRED_REGISTER.register("falling_block_but_mini_lol", () -> EntityType.Builder.of(com.brickmasterhunt.pfb.entity.PhysicsFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .build(new ResourceLocation(pfb.MOD_ID, "falling_block_but_mini_lol").toString()));

    public static final RegistryObject<EntityType<PhysicsFallingBlockEntity>> DAMN_THAT_BLOCK_IS_BIG =
            ENTITY_TYPE_DEFERRED_REGISTER.register("damn_that_block_be_lookin_kinda_thicc", () -> EntityType.Builder.of(com.brickmasterhunt.pfb.entity.PhysicsFallingBlockEntity::new, MobCategory.MISC)
                    .sized(20.0F, 20.0F)
                    .build(new ResourceLocation(pfb.MOD_ID, "damn_that_block_be_lookin_kinda_thicc").toString()));


    public static void register(IEventBus eventBus) {ENTITY_TYPE_DEFERRED_REGISTER.register(eventBus);}
}