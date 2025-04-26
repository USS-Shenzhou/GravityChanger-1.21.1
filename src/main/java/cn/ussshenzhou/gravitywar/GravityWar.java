package cn.ussshenzhou.gravitywar;

import cn.ussshenzhou.gravitywar.mek.GravityWarConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod(GravityWar.MODID)
public class GravityWar {
    public static final String MODID = "gravitywar";

    public GravityWar(IEventBus modEventBus, ModContainer modContainer) {
        ConfigHelper.loadConfig(new GravityWarConfig());
        if (ModList.get().isLoaded("sodium")) {
            throw new ModLoadingException(ModLoadingIssue.error("不支持Sodium"));
        }
    }
}
