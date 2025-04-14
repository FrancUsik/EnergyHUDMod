package franc.energyhud;

import com.example.energyhud.PacketRegistry;
import franc.energyhud.network.EnergyDataPacket;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = EnergyHUDMod.MODID, name = EnergyHUDMod.NAME, version = EnergyHUDMod.VERSION)
public class EnergyHUDMod {
    public static final String MODID = "energyhud";
    public static final String NAME = "Energy HUD";
    public static final String VERSION = "1.0";

    public static SimpleNetworkWrapper network;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        PacketRegistry.registerPackets();
    }
}
