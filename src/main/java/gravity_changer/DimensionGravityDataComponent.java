package gravity_changer;

import net.minecraft.core.HolderLookup;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class DimensionGravityDataComponent implements Component, AutoSyncedComponent {
    double dimensionGravityStrength = 1;
    
    private final Level currentWorld;
    
    public DimensionGravityDataComponent(Level world) {
        this.currentWorld = world;
    }
    
    public double getDimensionGravityStrength() {
        return dimensionGravityStrength;
    }
    
    public void setDimensionGravityStrength(double strength) {
        if (!currentWorld.isClientSide) {
            dimensionGravityStrength = strength;
            GravityChangerComponents.DIMENSION_COMP_KEY.sync(currentWorld);
        }
    }
    
    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        dimensionGravityStrength = tag.getDouble("DimensionGravityStrength");
    }
    
    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putDouble("DimensionGravityStrength", dimensionGravityStrength);
    }
}
