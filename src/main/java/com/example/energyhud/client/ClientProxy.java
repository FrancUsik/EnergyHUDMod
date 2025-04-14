package com.example.energyhud.client;

import com.example.energyhud.CommonProxy;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new HiTechHUDRenderer());
    }
}
