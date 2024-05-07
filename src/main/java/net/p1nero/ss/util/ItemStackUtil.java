package net.p1nero.ss.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.SwordSoaring;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * 由于物品的数据得存在物品栈，于是写了个辅助类
 * 你问我为什么数据要保存在ItemStack里而不是entity里？
 * 因为我本来是没打算普及所有Sword的的。。
 * Since the data of the item must exist in the item stack, an auxiliary class was written
 * You asked me why the data needs to be saved in itemStack instead of entity?
 * Because I didn't originally plan on popularizing all Swords...
 */
public class ItemStackUtil {

    //最长记录向量的时间
    public static final int maxRecordTick = 100;
    //最大灵力值
    public static final int maxSpiritValue = 10000;
    public static boolean isFlying(ItemStack sword) {
        return sword.getOrCreateTag().getBoolean("isFlying");
    }

    public static void setFlying(ItemStack sword, boolean isFlying) {
        sword.getOrCreateTag().putBoolean("isFlying", isFlying);
    }

    public static double getFlySpeedScale(ItemStack sword) {
        return sword.getOrCreateTag().getDouble("flySpeedScale");
    }

    public static void setFlySpeedScale(ItemStack sword, double flySpeedScale) {
        sword.getOrCreateTag().putDouble("flySpeedScale", flySpeedScale);
    }

    public static int getLeftTick(CompoundTag sword) {
        return sword.getInt("leftTick");
    }


    public static int getLeftTick(ItemStack sword) {
        return sword.getOrCreateTag().getInt("leftTick");
    }

    public static void setLeftTick(CompoundTag sword, int leftTick) {
        if(leftTick<0){
            return;
        }
        sword.putInt("leftTick", Math.min(leftTick, maxRecordTick));
    }

    public static void setLeftTick(ItemStack sword, int leftTick) {
        setLeftTick(sword.getOrCreateTag(), leftTick);
    }

    /**
     *用复合nbt标签优化，防止剑上下窜
     */
    public static int getSpiritValue(ItemStack sword) {
        return sword.getOrCreateTag().getList("spiritValue",Tag.TAG_COMPOUND).getCompound(0).getInt("spiritValue");
    }

    /**
     *用复合nbt标签优化，防止剑上下窜
     */
    public static void setSpiritValue(ItemStack sword, int spiritValue) {
        if(spiritValue<0 || spiritValue > maxSpiritValue){
            return;
        }
        if (!sword.getOrCreateTag().contains("spiritValue")) {
            ListTag tagsList = new ListTag();
            tagsList.add(new CompoundTag());
            sword.getOrCreateTag().put("spiritValue", tagsList);
        }
        sword.getOrCreateTag().getList("spiritValue", Tag.TAG_COMPOUND).getCompound(0).putInt("spiritValue", spiritValue);
    }

    public static Vec3 getEndVec(CompoundTag tag) {
        return new Vec3(tag.getDouble("endX"),tag.getDouble("endY"),tag.getDouble("endZ"));
    }

    public static Vec3 getEndVec(ItemStack sword) {
        return getEndVec(sword.getOrCreateTag());
    }

    public static void setEndVec(CompoundTag tag, Vec3 endVec) {
        tag.putDouble("endX", endVec.x);
        tag.putDouble("endY", endVec.y);
        tag.putDouble("endZ", endVec.z);
    }
    public static void setEndVec(ItemStack sword, Vec3 endVec) {
        setEndVec(sword.getOrCreateTag(), endVec);
    }

    /**
     * 进行停止飞行的一系列操作，设置飞行状态为false，设置末速度，设置结束飞行缓冲时间。
     * TODO 向下倾斜模拟重力
     */
    public static void stopFly(ItemStack sword){
        setFlying(sword,false);
        Vec3 endVec = getViewVec(sword,1).scale(getFlySpeedScale(sword));
        setEndVec(sword, endVec);
        double leftTick = endVec.length() * maxRecordTick / 2;
        setLeftTick(sword, ((int) leftTick));
    }

    /**
     * 获取前n个tick前的方向向量
     * 懒得重写了，直接用Player的persistentData吧
     */
    public static Vec3 getViewVec(CompoundTag sword, int tickBefore){
        if(tickBefore > maxRecordTick){
            return Vec3.ZERO;
        }
        checkOrCreateTag(sword);
        return getQueue(sword).toArray(new Vec3[maxRecordTick])[maxRecordTick -tickBefore];
    }

    public static Vec3 getViewVec(ItemStack sword, int tickBefore){
        return getViewVec(sword.getOrCreateTag(),tickBefore);
    }

    /**
     * 获取有记录的最初向量
     */
    public static Vec3 getViewVec(CompoundTag sword){
        return getQueue(sword).peek();
    }
    public static Vec3 getViewVec(ItemStack sword){
        return getQueue(sword.getOrCreateTag()).peek();
    }

    /**
     * 保存很多个tick前的方向向量，实现惯性效果
     * 通过队列来保存。
     * 并作插值，实现惯性漂移（太妙了）
     */
    public static void updateViewVec(CompoundTag sword, Vec3 viewVec){
        checkOrCreateTag(sword);
        Queue<Vec3> tickValues = getQueue(sword);
        tickValues.add(viewVec);
        Vec3 old = tickValues.poll();
        Queue<Vec3> newTickValues = new ArrayDeque<>();
        for(double i = 1; i <= tickValues.size(); i++){
            Vec3 newVec3 = old.lerp(viewVec, i / tickValues.size());
            newTickValues.add(newVec3);
        }
        saveQueue(sword, newTickValues);
    }

    public static void updateViewVec(ItemStack sword, Vec3 viewVec){
        updateViewVec(sword.getOrCreateTag(), viewVec);
    }

    /**
     * 获取前几个tick内的方向向量队列
     */
    public static Queue<Vec3> getQueue(CompoundTag sword){
        CompoundTag tag = checkOrCreateTag(sword);
        Queue<Vec3> tickValues = new ArrayDeque<>();
        for(int i = 0; i < maxRecordTick; i++){
            CompoundTag tickVec = tag.getList("view_vec_queue", Tag.TAG_COMPOUND).getCompound(i);
            tickValues.add(new Vec3(tickVec.getDouble("x"),tickVec.getDouble("y"),tickVec.getDouble("z")));
        }
        return tickValues;
    }

    /**
     * 保存前几个tick内的方向向量队列
     */
    public static void saveQueue(CompoundTag sword, Queue<Vec3> tickValues){
        CompoundTag tag = checkOrCreateTag(sword);
        for(int i = 0; i < maxRecordTick; i++){
            CompoundTag tickVecTag = tag.getList("view_vec_queue", Tag.TAG_COMPOUND).getCompound(i);
            Vec3 tickVec = tickValues.remove();
            tickVecTag.putDouble("x", tickVec.x);
            tickVecTag.putDouble("y", tickVec.y);
            tickVecTag.putDouble("z", tickVec.z);
        }
    }

    /**
     * 检查是否为空标签，是则创建一个完备的给它。防止异常。
     */
    public static CompoundTag checkOrCreateTag(CompoundTag tag){
        if (!tag.contains("view_vec_queue")) {
            ListTag tickTagsList = new ListTag();
            for (int i = 0; i < maxRecordTick; i++) {
                tickTagsList.add(new CompoundTag());
            }
            tag.put("view_vec_queue", tickTagsList);
        }
        return tag;
    }

    /**
     * 搜索所有物品栈
     * @return 返回物品栈
     */
    public static List<ItemStack> searchItem(Player player, Item item, Predicate<ItemStack> predicate) {
        List<ItemStack> list = new ArrayList<>();
        if (item == player.getMainHandItem().getItem() && predicate.test(player.getMainHandItem())) {
            list.add(player.getMainHandItem());
        } else if (item == player.getOffhandItem().getItem()&& predicate.test(player.getOffhandItem())) {
            list.add(player.getOffhandItem());
        } else {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack teststack = player.getInventory().items.get(i);
                if (teststack != null && teststack.getItem() == item && predicate.test(teststack)) {
                    list.add(teststack);
                }
            }
        }
        return list;
    }

    /**
     * 搜索所有剑所在的物品栈
     * @return 返回物品栈
     */
    public static List<ItemStack> searchSwordItem(Player player, Item item) {
        return searchItem(player, item,(itemStack)->true);
    }

    /**
     * 搜索所有符合条件的剑所在的物品栈
     * @return 返回物品栈
     */
    public static List<ItemStack> searchSwordItem(Player player, Predicate<ItemStack> predicate) {
        List<ItemStack> list = new ArrayList<>();
        if (SwordSoaring.isValidSword(player.getMainHandItem()) && predicate.test(player.getMainHandItem())) {
            list.add(player.getMainHandItem());
        } else if (SwordSoaring.isValidSword(player.getOffhandItem()) && predicate.test(player.getOffhandItem())) {
            list.add(player.getOffhandItem());
        } else {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack teststack = player.getInventory().items.get(i);
                if (SwordSoaring.isValidSword(teststack) && predicate.test(teststack)) {
                    list.add(teststack);
                }
            }
        }
        return list;
    }

}
