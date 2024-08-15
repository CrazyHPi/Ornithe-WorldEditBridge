package webridge;

import com.sk89q.worldedit.world.registry.BiomeRegistry;
import com.sk89q.worldedit.world.registry.LegacyWorldData;

/**
 * World data for the Carpet platform.
 */
class VanillaWorldData extends LegacyWorldData {

    private static final VanillaWorldData INSTANCE = new VanillaWorldData();
    private final BiomeRegistry biomeRegistry = new VanillaBiomeRegistry();

    /**
     * Create a new instance.
     */
    VanillaWorldData() {
    }

    @Override
    public BiomeRegistry getBiomeRegistry() {
        return biomeRegistry;
    }

    /**
     * Get a static instance.
     *
     * @return an instance
     */
    public static VanillaWorldData getInstance() {
        return INSTANCE;
    }

}
