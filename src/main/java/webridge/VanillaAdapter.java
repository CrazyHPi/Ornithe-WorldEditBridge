package webridge;

import com.sk89q.worldedit.world.World;

final class VanillaAdapter {

    private VanillaAdapter() {
    }

    public static World adapt(net.minecraft.world.World world) {
        return new VanillaWorld(world);
    }

}
