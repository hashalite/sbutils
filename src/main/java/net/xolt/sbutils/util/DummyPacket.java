package net.xolt.sbutils.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public class DummyPacket extends ServerboundUseItemOnPacket {

    public DummyPacket(InteractionHand hand, BlockHitResult blockHit, int sequence) {
        super(hand, blockHit, sequence);
    }
}