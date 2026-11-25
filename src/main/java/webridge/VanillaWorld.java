package webridge;

import com.google.common.io.Files;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.gen.ChunkProviderServer;
import webridge.mixins.accessors.ChunkProviderServerAccessor;
import webridge.mixins.accessors.MinecraftServerAccessor;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
@SuppressWarnings("deprecation")
class VanillaWorld extends AbstractWorld {

    private static final Logger logger = Logger.getLogger(VanillaWorld.class.getCanonicalName());
    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    VanillaWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<World>(world);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws WorldEditException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorldChecked() throws WorldEditException {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldReferenceLostException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorld() {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return getWorld().getWorldInfo().getWorldName();
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        Chunk chunk = world.getChunk(x >> 4, z >> 4);
        IBlockState oldState = Blocks.AIR.getDefaultState();

        if (notifyAndLight) {
            oldState = chunk.getBlockState(x & 15, y, z & 15);
        }

        IBlockState newState = Block.getBlockById(block.getId()).getStateFromMeta(block.getData());
        boolean successful = chunk.setBlockState(new BlockPos(x, y, z), newState) != null;

        // Create the TileEntity
        if (successful) {
            CompoundTag tag = block.getNbtData();
            if (tag != null) {
                NBTTagCompound nativeTag = NBTConverter.toNative(tag);
                nativeTag.setString("id", block.getNbtId());
                TileEntityUtils.setTileEntity(getWorld(), position, nativeTag);
            }
        }

        BlockPos pos = new BlockPos(x, y, z);
        if (notifyAndLight) {
            if (newState.getLightOpacity() != oldState.getLightOpacity() || newState.getLightValue() != oldState.getLightValue())
                world.checkLight(pos);
        }
        world.notifyBlockUpdate(pos, oldState, newState, 3);
        if (notifyAndLight) {
            world.notifyNeighborsRespectDebug(pos, oldState.getBlock(), true);

            if (newState.hasComparatorInputOverride()) {
                world.updateComparatorOutputLevel(pos, newState.getBlock());
            }
        }

        return successful;
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        checkNotNull(position);
        return getWorld().getLightFor(EnumSkyBlock.BLOCK, new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        checkNotNull(position);
        TileEntity tile = getWorld().getTileEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        if ((tile instanceof IInventory)) {
            IInventory inv = (IInventory) tile;
            int size = inv.getSizeInventory();
            for (int i = 0; i < size; i++) {
                inv.setInventorySlotContents(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        checkNotNull(position);
        return new BaseBiome(Biome.getIdForBiome(getWorld().getBiome(new BlockPos(position.getBlockX(), 0, position.getBlockZ()))));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        checkNotNull(position);
        checkNotNull(biome);

        BlockPos pos = new BlockPos(position.getBlockX(), 0, position.getBlockZ());
        if (getWorld().isBlockLoaded(pos)) {
            Chunk chunk = getWorld().getChunk(pos);
            chunk.getBiomeArray()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = (byte) biome.getId();
            return true;
        }

        return false;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == 0) {
            return;
        }

        EntityItem entity = new EntityItem(getWorld(), position.getX(), position.getY(), position.getZ(), VanillaWorldEdit.toCarpetItemStack(item));
        entity.setDefaultPickupDelay();
        getWorld().spawnEntity(entity);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // Don't even try to regen if it's going to fail.
        IChunkProvider provider = getWorld().getChunkProvider();
        if (!(provider instanceof ChunkProviderServer)) {
            return false;
        }

        File saveFolder = Files.createTempDir();
        // register this just in case something goes wrong
        // normally it should be deleted at the end of this method
        saveFolder.deleteOnExit();

        WorldServer originalWorld = (WorldServer) getWorld();

        MinecraftServer server = originalWorld.getMinecraftServer();
        AnvilSaveHandler saveHandler = new AnvilSaveHandler(saveFolder,
                originalWorld.getSaveHandler().getWorldDirectory().getName(), true, ((MinecraftServerAccessor) server).getDataFixer());
        World freshWorld = new WorldServer(server, saveHandler, originalWorld.getWorldInfo(),
                originalWorld.provider.getDimensionType().getId(), originalWorld.profiler).init();

        // Pre-gen all the chunks
        // We need to also pull one more chunk in every direction
        CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 0, 16), region.getMaximumPoint().add(16, 0, 16));
        for (Vector2D chunk : expandedPreGen.getChunks()) {
            freshWorld.getChunk(chunk.getBlockX(), chunk.getBlockZ());
        }

        VanillaWorld from = new VanillaWorld(freshWorld);
        try {
            for (BlockVector vec : region) {
                editSession.setBlock(vec, from.getBlock(vec));
            }
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        } finally {
            saveFolder.delete();
        }

        return true;
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public WorldData getWorldData() {
        return VanillaWorldData.getInstance();
    }

    @Override
    public boolean isValidBlockType(int id) {
        return (id == 0) || (Block.getBlockById(id) != null);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        World world = getWorld();
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getZ());
        IBlockState state = world.getBlockState(pos);
        int id = Block.getIdFromBlock(state.getBlock());
        int data = state.getBlock().getMetaFromState(state);
        TileEntity tile = getWorld().getTileEntity(pos);

        if (tile != null) {
            return new TileEntityBaseBlock(id, data, tile);
        } else {
            return new BaseBlock(id, data);
        }
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        IBlockState state = world.getBlockState(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        int id = Block.getIdFromBlock(state.getBlock());
        int data = state.getBlock().getMetaFromState(state);
        return new LazyBlock(id, data, this, position);
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if ((o instanceof VanillaWorld)) {
            VanillaWorld other = ((VanillaWorld) o);
            World otherWorld = other.worldRef.get();
            World thisWorld = worldRef.get();
            return otherWorld != null && thisWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList<Entity>();
        World world = getWorld();
        List<net.minecraft.entity.Entity> ents = world.loadedEntityList;
        for (net.minecraft.entity.Entity entity : ents) {
            if (region.contains(new Vector(entity.posX, entity.posY, entity.posZ))) {
                entities.add(new VanillaEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList<Entity>();
        for (Object entity : getWorld().loadedEntityList) {
            entities.add(new VanillaEntity((net.minecraft.entity.Entity) entity));
        }
        return entities;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();
        net.minecraft.entity.Entity createdEntity = EntityList.createEntityByIDFromName(new ResourceLocation(entity.getTypeId()), world);
        if (createdEntity != null) {
            CompoundTag nativeTag = entity.getNbtData();
            if (nativeTag != null) {
                NBTTagCompound tag = NBTConverter.toNative(entity.getNbtData());
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.removeTag(name);
                }
                createdEntity.readFromNBT(tag);
            }

            createdEntity.setLocationAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            world.spawnEntity(createdEntity);
            return new VanillaEntity(createdEntity);
        } else {
            return null;
        }
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    @SuppressWarnings("serial")
    private static class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

}
