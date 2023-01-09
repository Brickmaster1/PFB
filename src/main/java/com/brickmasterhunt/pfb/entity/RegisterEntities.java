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

    public static final RegistryObject<EntityType<TestFallingBlockEntity>> TEST_FALLING_BLOCK =
            ENTITY_TYPE_DEFERRED_REGISTER.register("test_falling_block", () -> EntityType.Builder.of(TestFallingBlockEntity::new, MobCategory.MISC).build(new ResourceLocation(pfb.MOD_ID, "test_falling_block").toString()));


    public static void register(IEventBus eventBus) {ENTITY_TYPE_DEFERRED_REGISTER.register(eventBus);}
}