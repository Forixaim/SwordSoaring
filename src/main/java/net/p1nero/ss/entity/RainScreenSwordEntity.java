package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 都用EntityDataAccessor了还继承有点没必要..但是不知道怎么抛弃EntityDataAccessor
 */
public class RainScreenSwordEntity extends SwordEntity{

    private static final EntityDataAccessor<Optional<UUID>> RIDER_UUID = SynchedEntityData.defineId(RainScreenSwordEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(RainScreenSwordEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> RAIN_SCREEN_SWORD_ID = SynchedEntityData.defineId(RainScreenSwordEntity.class, EntityDataSerializers.INT);

    public RainScreenSwordEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
        this.getEntityData().define(RIDER_UUID, Optional.empty());
        this.getEntityData().define(RAIN_SCREEN_SWORD_ID, -1);
    }
    public RainScreenSwordEntity(ItemStack itemStack, Player rider, int swordID) {
        super(itemStack, rider);
        this.rider = rider;
        this.getEntityData().define(ITEM_STACK, itemStack);
        this.getEntityData().define(RIDER_UUID, Optional.of(rider.getUUID()));
        this.getEntityData().define(RAIN_SCREEN_SWORD_ID, swordID);
    }

    @Override
    public ItemStack getItemStack() {
        return this.getEntityData().get(ITEM_STACK);
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        this.getEntityData().set(ITEM_STACK, itemStack);
    }

    @Override
    public void setRider(Player rider) {
        this.rider = rider;
        this.getEntityData().set(RIDER_UUID, Optional.of(rider.getUUID()));
    }

    public void setSwordID(int swordID){
        getEntityData().set(RAIN_SCREEN_SWORD_ID, swordID);
    }

    public int getRainScreenSwordId() {
        return getEntityData().get(RAIN_SCREEN_SWORD_ID);
    }

    public Vec3 getOffset(){
        double dis = 1.3;
        return switch (getRainScreenSwordId()){
            case 0 -> new Vec3(-dis,1,0);
            case 1 -> new Vec3(0,1,-dis);
            case 2 -> new Vec3(dis,1,0);
            case 3 -> new Vec3(0,1,dis);
            default -> new Vec3(0,1,0);
        };
    }

    @Override
    public void tick() {

        //想办法不让rider为null
        if(rider == null){
            //呃呃呃简单粗暴
            if(level().isClientSide){
                rider = Minecraft.getInstance().player;
            }
            if(this.getEntityData().get(RIDER_UUID).isPresent()){
                rider = level().getPlayerByUUID(this.getEntityData().get(RIDER_UUID).get());
            }else {
                SwordSoaring.LOGGER.info("sword entity "+ getId() + " doesn't have rider "+level());
                discard();
                return;
            }
        }

        //无能为力，那就紫砂吧
        if(rider == null){
            SwordSoaring.LOGGER.info("sword entity "+ getId() + " doesn't have rider "+level());
            discard();
            return;
        }

        //围绕rider旋转
        Vec3 center = rider.getPosition(0.5f);
        Vec3 now = center.add(getOffset());
        double radians = tickCount * 0.1;
        double rotatedX = center.x + (float) (Math.cos(radians) * (now.x - center.x) - Math.sin(radians) * (now.z - center.z));
        double rotatedZ = center.z + (float) (Math.sin(radians) * (now.x - center.x) + Math.cos(radians) * (now.z - center.z));
        setPos(new Vec3(rotatedX, now.y+Math.sin(radians)*0.1, rotatedZ));

        SSPlayer ssPlayer = rider.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
        if(this.tickCount > 200){
            ssPlayer.setSwordScreenEntityCount(ssPlayer.getSwordScreenEntityCount() - 1);
            discard();
        }

        //触发技能，撞到实体造成伤害，自身回血
        List<Entity> entities = level().getEntities(this, new AABB(getPosition(0).add(-5,-5,-5), getPosition(0).add(5,5,5))
                , entity -> entity.getBoundingBox().contains(getPosition(0.5f)));
        for (Entity entity : entities){
            if(entity.getId() == rider.getId()){
                continue;
            }
            entity.hurt(damageSources().playerAttack(rider), 1f);
            rider.heal(1f);
            ssPlayer.setSwordScreenEntityCount(ssPlayer.getSwordScreenEntityCount()-1);
            discard();
            return;
        }

    }



    @Override
    public void setPose(PoseStack poseStack) {
        Item sword = getItemStack().getItem();
        if(SwordSoaring.epicFightLoad() && (sword instanceof UchigatanaItem || sword instanceof TachiItem || sword instanceof LongswordItem)){
            poseStack.mulPose(Axis.ZP.rotationDegrees(135));
        }else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-225));
        }
    }
}