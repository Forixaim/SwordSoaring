package net.p1nero.ss.keymapping;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
@Mod.EventBusSubscriber(value = {Dist.CLIENT},bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeyMappings {
    public static final MyKeyMapping CHANGE_SPEED = new MyKeyMapping("key.sword_soaring.change_speed", GLFW.GLFW_KEY_TAB, "key.sword_soaring.combat");
    public static final MyKeyMapping FLY = new MyKeyMapping("key.sword_soaring.fly", GLFW.GLFW_KEY_SPACE, "key.sword_soaring.combat");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CHANGE_SPEED);
        event.register(FLY);
    }

}
