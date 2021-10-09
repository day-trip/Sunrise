package net.minecraft.network;

import net.minecraft.util.IThreadListener;

public class PacketThreadUtil
{
    public static <T extends INetHandler> void checkThreadAndEnqueue(Packet<T> packet, T handler, IThreadListener threadListener) throws ThreadQuickExitException
    {
        if (!threadListener.isCallingFromMinecraftThread())
        {
            threadListener.addScheduledTask(() -> packet.processPacket(handler));
            throw ThreadQuickExitException.instance;
        }
    }
}
