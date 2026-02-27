package org.creepebucket.programmable_magic.renderer.api;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class ModVec3 extends Vec3 {
    public ModVec3(double x, double y, double z) {
        super(x, y, z);
    }

    public ModVec3(Vector3fc vector) {
        super(vector);
    }

    public ModVec3(Vec3i vector) {
        super(vector);
    }

    public Vec3 multiply(double n) {
        return this.multiply(n, n, n);
    }
}
