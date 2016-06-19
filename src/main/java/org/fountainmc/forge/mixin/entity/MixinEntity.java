/*
 * This file is part of FountainForge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Fountain
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.fountainmc.forge.mixin.entity;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import org.fountainmc.api.entity.Entity;
import org.fountainmc.api.world.Location;
import org.fountainmc.api.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.entity.Entity.class)
@Implements(@Interface(iface = Entity.class, prefix = "entity$"))
public abstract class MixinEntity implements Entity, ICommandSender {

    @Shadow public boolean onGround;

    @Shadow public float rotationPitch;

    @Shadow public float rotationYaw;

    @Shadow
    public abstract BlockPos getPosition();

    @Shadow
    public abstract net.minecraft.world.World getEntityWorld();

    @Shadow
    public abstract void setPosition(double x, double y, double z);

    @Shadow
    public abstract List<net.minecraft.entity.Entity> shadow$getPassengers();

    @Shadow
    public abstract void shadow$addPassenger(net.minecraft.entity.Entity passenger);

    @Shadow
    public abstract void removePassenger(net.minecraft.entity.Entity passenger);

    @Override
    public Location getLocation() {
        return new Location((World) this.getEntityWorld(),
                this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ());
    }

    @Override
    public void teleport(Location destination) {
        this.setPosition(destination.getX(), destination.getY(),
                destination.getZ());
    }

    @Override
    public float getPitch() {
        return this.rotationPitch;
    }

    @Override
    public void setPitch(float pitch) {
        this.rotationPitch = pitch;
    }

    @Override
    public float getYaw() {
        return this.rotationYaw;
    }

    @Override
    public void setYaw(float yaw) {
        this.rotationYaw = yaw;
    }

    @Override
    public boolean isOnGround() {
        return this.onGround;
    }

    @Nullable
    @Override
    public Entity getPrimaryPassenger() {
        if (getPassengers().size() != 0) {
            return getPassengers().get(0);
        }
        return null;
    }

    @Nonnull
    public ImmutableList<Entity> entity$getPassengers() {
        List<net.minecraft.entity.Entity> entities = this.shadow$getPassengers();
        List<Entity> apiEntities = new ArrayList<>();
        for (net.minecraft.entity.Entity entity : entities) {
            apiEntities.add((Entity) entity);
        }
        return ImmutableList.copyOf(apiEntities);
    }

    @Override
    public void setPrimaryPassenger(Entity passenger) {
        this.ejectPassenger(this.getPrimaryPassenger());
        ((net.minecraft.entity.Entity) passenger).startRiding(this.getCommandSenderEntity());
    }

    @Override
    public boolean addPassenger(Entity entity, boolean force) {
        this.shadow$addPassenger((net.minecraft.entity.Entity) entity);
        return true;
    }

    @Override
    public void ejectAll() {
        this.getPassengers().forEach(this::ejectPassenger);
    }

    @Override
    public void ejectPassenger(Entity passenger) {
        passenger.leaveVehicle();
    }

    @Override
    public int getMaximumPassengers() {
        return this.shadow$getPassengers().size();
    }

    @Override
    public void dismountVehicle() {
        if (this.shadow$getPassengers().size() != 0) { return; }
        this.getPrimaryPassenger().leaveVehicle();
    }

    @Nullable
    @Override
    public Entity getVehicle() {
        return (Entity) this.getCommandSenderEntity().getRidingEntity();
    }

    @Override
    public void leaveVehicle() {
        (this.getCommandSenderEntity()).dismountRidingEntity();
    }

    @Override
    public ImmutableCollection<Entity> getNearbyEntities(double distance) {
        List<net.minecraft.entity.Entity> nmsEntities = this.getCommandSenderEntity().worldObj.getEntitiesWithinAABBExcludingEntity(this.getCommandSenderEntity(), this.getCommandSenderEntity().getEntityBoundingBox().expand(distance / 3, distance / 3, distance / 3));
        List<Entity> apiEntities = new ArrayList<>();
        for (net.minecraft.entity.Entity entity : nmsEntities) {
            apiEntities.add((Entity) entity);
        }
        return ImmutableList.copyOf(apiEntities);
    }

}
