package com.brickmasterhunt.pfb;

import com.brickmasterhunt.pfb.entity.PhysicsFallingBlockEntity;
import com.brickmasterhunt.pfb.entity.RegisterEntities;
import com.brickmasterhunt.pfb.entity.render.PhysicsFallingBlockRenderer;
import com.jme3.math.Vector3f;
import com.mojang.logging.LogUtils;
import dev.lazurite.rayon.api.PhysicsElement;
import dev.lazurite.rayon.api.event.collision.ElementCollisionEvents;
import dev.lazurite.rayon.impl.bullet.collision.body.TerrainRigidBody;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(pfb.MOD_ID)
public class pfb {
    public static final String MOD_ID = "pfb";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public pfb() {
        IEventBus eventBus =FMLJavaModLoadingContext.get().getModEventBus();
        RegisterEntities.register(eventBus);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        pfb.initialize();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("PFB", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    public static void initialize() {
        LOGGER.info("Initializing PFB");
        ElementCollisionEvents.BLOCK_COLLISION.register(pfb::onCollision);
    }

    public static void onCollision(PhysicsElement element, TerrainRigidBody terrain, float impulse) {
        if (element instanceof PhysicsFallingBlockEntity physicsFallingBlockEntity) {

            if (terrain.getBlockState().getBlock().equals(Blocks.BRICKS)) {
                System.out.println("Collided with bricks!");
                PhysicsFallingBlockEntity newEntity = RegisterEntities.FALLING_BLOCK.get().create(physicsFallingBlockEntity.getLevel());
                Vector3f elementVelocity = element.getRigidBody().getLinearVelocity(new Vector3f());
                Vector3f negVel = elementVelocity.multLocal(-1,-1,-1);
                newEntity.getRigidBody().setLinearVelocity(negVel);
                physicsFallingBlockEntity.getLevel().addFreshEntity(newEntity);
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        private static EntityType<FallingBlockEntity> TEST_FALLING_BLOCK;

        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // Register a new block here
            LOGGER.info("HELLO from Register Block");
        }

        @SubscribeEvent
        public static void onInitializeClient(FMLClientSetupEvent event) {
            EntityRenderers.register(RegisterEntities.FALLING_BLOCK.get(), PhysicsFallingBlockRenderer::new);
            EntityRenderers.register(RegisterEntities.FALLING_BLOCK_BUT_MINI_LOL.get(), PhysicsFallingBlockRenderer::new);
            EntityRenderers.register(RegisterEntities.DAMN_THAT_BLOCK_IS_BIG.get(), PhysicsFallingBlockRenderer::new);
        }
    }
}
