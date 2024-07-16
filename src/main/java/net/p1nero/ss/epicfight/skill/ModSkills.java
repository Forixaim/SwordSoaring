package net.p1nero.ss.epicfight.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.p1nero.ss.SwordSoaring;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;

public class ModSkills {

    public static Skill SWORD_SOARING;
    public static Skill RAIN_SCREEN;
    public static Skill RAIN_CUTTER;
//    public static Skill YAKSHA_MASK;
    public static Skill STELLAR_RESTORATION;

    public static CreativeModeTab swordSoaringTab = CreativeModeTab.builder().title(Component.translatable("key.sword_soaring.combat")).icon(Items.DIAMOND_SWORD::getDefaultInstance).build();

    public static void registerSkills() {
        SkillManager.register(RainCutter::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.IDENTITY).setCreativeTab(swordSoaringTab), SwordSoaring.MOD_ID, "rain_cutter");
        SkillManager.register(RainScreen::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.GUARD).setCreativeTab(swordSoaringTab), SwordSoaring.MOD_ID, "rain_screen");
//        SkillManager.register(YakshaMask::new, YakshaMask.createYakshaMaskBuilder(), SwordSoaring.MOD_ID, "yaksha_mask");
        SkillManager.register(StellarRestoration::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.DODGE).setCreativeTab(swordSoaringTab), SwordSoaring.MOD_ID, "stellar_restoration");
        SkillManager.register(SwordSoaringSkill::new, Skill.createMoverBuilder().setResource(Skill.Resource.NONE).setCreativeTab(swordSoaringTab), SwordSoaring.MOD_ID, "sword_soaring");
    }

    public static void BuildSkills(SkillBuildEvent event){
        RAIN_CUTTER = event.build(SwordSoaring.MOD_ID, "rain_cutter");
        SWORD_SOARING = event.build(SwordSoaring.MOD_ID, "sword_soaring");
//        YAKSHA_MASK = event.build(SwordSoaring.MOD_ID, "yaksha_mask");
        RAIN_SCREEN =  event.build(SwordSoaring.MOD_ID, "rain_screen");
        STELLAR_RESTORATION = event.build(SwordSoaring.MOD_ID, "stellar_restoration");
    }

}
