/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.block.storage;

import javax.annotation.Nullable;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.SkyChestContainer;
import appeng.tile.storage.SkyChestBlockEntity;
import appeng.util.Platform;

public class SkyChestBlock extends AEBaseTileBlock<SkyChestBlockEntity> {

    private static final double AABB_OFFSET_BOTTOM = 0.00;
    private static final double AABB_OFFSET_SIDES = 0.06;
    private static final double AABB_OFFSET_TOP = 0.125;

    public enum SkyChestType {
        STONE, BLOCK
    };

    public final SkyChestType type;

    public SkyChestBlock(final SkyChestType type, Settings props) {
        super(props);
        this.type = type;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (Platform.isServer()) {
            SkyChestBlockEntity tile = getBlockEntity(w, pos);
            if (tile == null) {
                return ActionResult.PASS;
            }

            ContainerOpener.openContainer(SkyChestContainer.TYPE, player, ContainerLocator.forTileEntity(tile));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        // TODO Cache this! It can't be that hard!
        Box aabb = computeAABB(worldIn, pos);
        return VoxelShapes.create(aabb);
    }

    private Box computeAABB(final BlockView w, final BlockPos pos) {
        final SkyChestBlockEntity sk = this.getBlockEntity(w, pos);
        Direction o = Direction.UP;

        if (sk != null) {
            o = sk.getUp();
        }

        final double offsetX = o.getOffsetX() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetY = o.getOffsetY() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetZ = o.getOffsetZ() == 0 ? AABB_OFFSET_SIDES : 0.0;

        // for x/z top and bottom is swapped
        final double minX = Math.max(0.0,
                offsetX + (o.getOffsetX() < 0 ? AABB_OFFSET_BOTTOM : (o.getOffsetX() * AABB_OFFSET_TOP)));
        final double minY = Math.max(0.0,
                offsetY + (o.getOffsetY() < 0 ? AABB_OFFSET_TOP : (o.getOffsetY() * AABB_OFFSET_BOTTOM)));
        final double minZ = Math.max(0.0,
                offsetZ + (o.getOffsetZ() < 0 ? AABB_OFFSET_BOTTOM : (o.getOffsetZ() * AABB_OFFSET_TOP)));

        final double maxX = Math.min(1.0,
                1.0 - offsetX - (o.getOffsetX() < 0 ? AABB_OFFSET_TOP : (o.getOffsetX() * AABB_OFFSET_BOTTOM)));
        final double maxY = Math.min(1.0,
                1.0 - offsetY - (o.getOffsetY() < 0 ? AABB_OFFSET_BOTTOM : (o.getOffsetY() * AABB_OFFSET_TOP)));
        final double maxZ = Math.min(1.0,
                1.0 - offsetZ - (o.getOffsetZ() < 0 ? AABB_OFFSET_TOP : (o.getOffsetZ() * AABB_OFFSET_BOTTOM)));

        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }
}