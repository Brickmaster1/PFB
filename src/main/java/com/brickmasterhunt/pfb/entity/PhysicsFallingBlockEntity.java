package com.brickmasterhunt.pfb.entity;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.mojang.logging.LogUtils;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class PhysicsFallingBlockEntity extends FallingBlockEntity implements EntityPhysicsElement {
    public boolean dropItem = true;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance;
    @Nullable
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    private static final Logger LOGGER = LogUtils.getLogger();
    private BlockState blockState = Blocks.SAND.defaultBlockState();
    private final EntityRigidBody rigidBody = new EntityRigidBody(this);
    private BoundingBox box;

    public PhysicsFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
        this.rigidBody.setDragCoefficient(0.001f);
        this.rigidBody.setMass(1.5f);
    }

    private PhysicsFallingBlockEntity(Level level, double x, double y, double z, BlockState blockState) {
        this(EntityType.FALLING_BLOCK, level);
        this.blockState = blockState;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
    }

    public void getVelocity() {
        getRigidBody().getLinearVelocity(new Vector3f());
    }

    public static PhysicsFallingBlockEntity fall(Level level, BlockPos blockPos, BlockState blockState) {
        PhysicsFallingBlockEntity physicsFallingBlockEntity = new PhysicsFallingBlockEntity(level, (double)blockPos.getX() + 0.5D, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5D, blockState.hasProperty(BlockStateProperties.WATERLOGGED) ? blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : blockState);
        level.setBlock(blockPos, blockState.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(physicsFallingBlockEntity);
        return physicsFallingBlockEntity;
    }

    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
        } else {
            Block block = this.blockState.getBlock();
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level.isClientSide) {
                BlockPos blockpos = this.blockPosition();
                boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean flag1 = flag && this.level.getFluidState(blockpos).is(FluidTags.WATER);
                double d0 = this.getDeltaMovement().lengthSqr();
                if (flag && d0 > 1.0D) {
                    BlockHitResult blockhitresult = this.level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                    if (blockhitresult.getType() != HitResult.Type.MISS && this.level.getFluidState(blockhitresult.getBlockPos()).is(FluidTags.WATER)) {
                        blockpos = blockhitresult.getBlockPos();
                        flag1 = true;
                    }
                }

                if (!this.onGround && !flag1) {
                    if (!this.level.isClientSide && (this.time > 100 && (blockpos.getY() <= this.level.getMinBuildHeight() || blockpos.getY() > this.level.getMaxBuildHeight()) || this.time > 600)) {
                        if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(block);
                        }

                        this.discard();
                    }
                } else {
                    BlockState blockstate = this.level.getBlockState(blockpos);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                    if (!blockstate.is(Blocks.MOVING_PISTON)) {
                        if (!this.cancelDrop) {
                            boolean flag2 = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level, blockpos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean flag3 = FallingBlock.isFree(this.level.getBlockState(blockpos.below())) && (!flag || !flag1);
                            boolean flag4 = this.blockState.canSurvive(this.level, blockpos) && !flag3;
                            if (flag2 && flag4) {
                                if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(blockpos).getType() == Fluids.WATER) {
                                    this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
                                }

                                if (this.level.setBlock(blockpos, this.blockState, 3)) {
                                    ((ServerLevel)this.level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.level.getBlockState(blockpos)));
                                    this.discard();
                                    if (block instanceof Fallable) {
                                        ((Fallable)block).onLand(this.level, blockpos, this.blockState, blockstate, this);
                                    }

                                    if (this.blockData != null && this.blockState.hasBlockEntity()) {
                                        BlockEntity blockentity = this.level.getBlockEntity(blockpos);
                                        if (blockentity != null) {
                                            CompoundTag compoundtag = blockentity.saveWithoutMetadata();

                                            for(String s : this.blockData.getAllKeys()) {
                                                compoundtag.put(s, this.blockData.get(s).copy());
                                            }

                                            try {
                                                blockentity.load(compoundtag);
                                            } catch (Exception exception) {
                                                LOGGER.error("Failed to load block entity from falling block", (Throwable)exception);
                                            }

                                            blockentity.setChanged();
                                        }
                                    }
                                } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.discard();
                                    this.callOnBrokenAfterFall(block, blockpos);
                                    this.spawnAtLocation(block);
                                }
                            } else {
                                this.discard();
                                if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.callOnBrokenAfterFall(block, blockpos);
                                    this.spawnAtLocation(block);
                                }
                            }
                        } else {
                            this.discard();
                            this.callOnBrokenAfterFall(block, blockpos);
                        }
                    }
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }
    }

    public boolean causeFallDamage(float p_149643_, float p_149644_, DamageSource damageSource) {
        if (!this.hurtEntities) {
            return false;
        } else {
            int i = Mth.ceil(p_149643_ - 1.0F);
            if (i < 0) {
                return false;
            } else {
                Predicate<Entity> predicate;
                DamageSource damagesource;
                if (this.blockState.getBlock() instanceof Fallable) {
                    Fallable fallable = (Fallable)this.blockState.getBlock();
                    predicate = fallable.getHurtsEntitySelector();
                    damagesource = fallable.getFallDamageSource();
                } else {
                    predicate = EntitySelector.NO_SPECTATORS;
                    damagesource = DamageSource.FALLING_BLOCK;
                }

                float f = (float)Math.min(Mth.floor((float)i * this.fallDamagePerDistance), this.fallDamageMax);
                this.level.getEntities(this, this.getBoundingBox(), predicate).forEach((p_149649_) -> {
                    p_149649_.hurt(damagesource, f);
                });
                boolean flag = this.blockState.is(BlockTags.ANVIL);
                if (flag && f > 0.0F && this.random.nextFloat() < 0.05F + (float)i * 0.05F) {
                    BlockState blockstate = AnvilBlock.damage(this.blockState);
                    if (blockstate == null) {
                        this.cancelDrop = true;
                    } else {
                        this.blockState = blockstate;
                    }
                }

                return false;
            }
        }
    }

    public void setStartPos(BlockPos p_31960_) {
        this.entityData.set(DATA_START_POS, p_31960_);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    public boolean isPickable() {
        return !this.isRemoved();
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        compoundTag.putInt("Time", this.time);
        compoundTag.putBoolean("DropItem", this.dropItem);
        compoundTag.putBoolean("HurtEntities", this.hurtEntities);
        compoundTag.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        compoundTag.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            compoundTag.put("TileEntityData", this.blockData);
        }

    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.blockState = NbtUtils.readBlockState(compoundTag.getCompound("BlockState"));
        this.time = compoundTag.getInt("Time");
        if (compoundTag.contains("HurtEntities", 99)) {
            this.hurtEntities = compoundTag.getBoolean("HurtEntities");
            this.fallDamagePerDistance = compoundTag.getFloat("FallHurtAmount");
            this.fallDamageMax = compoundTag.getInt("FallHurtMax");
        } else if (this.blockState.is(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }

        if (compoundTag.contains("DropItem", 99)) {
            this.dropItem = compoundTag.getBoolean("DropItem");
        }

        if (compoundTag.contains("TileEntityData", 10)) {
            this.blockData = compoundTag.getCompound("TileEntityData");
        }

        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }

    }

    public void setHurtsEntities(float fallDamagePerDistance, int fallDamageMax) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = fallDamagePerDistance;
        this.fallDamageMax = fallDamageMax;
    }

    public boolean displayFireAnimation() {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        super.fillCrashReportCategory(crashReportCategory);
        crashReportCategory.setDetail("Imitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }
}