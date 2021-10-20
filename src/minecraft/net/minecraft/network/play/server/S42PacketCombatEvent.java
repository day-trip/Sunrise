package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.CombatTracker;

public class S42PacketCombatEvent implements Packet<INetHandlerPlayClient>
{
    public S42PacketCombatEvent.Event eventType;
    public int attackerEntityID;
    public int targetEntityID;
    public int ticksExisted;
    public String deathMessage;

    public S42PacketCombatEvent()
    {
    }

    public S42PacketCombatEvent(CombatTracker combatTrackerIn, S42PacketCombatEvent.Event combatEventType)
    {
        eventType = combatEventType;
        EntityLivingBase entitylivingbase = combatTrackerIn.func_94550_c();

        switch (combatEventType)
        {
            case END_COMBAT:
                ticksExisted = combatTrackerIn.func_180134_f();
                targetEntityID = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
                break;

            case ENTITY_DIED:
                attackerEntityID = combatTrackerIn.getFighter().getEntityId();
                targetEntityID = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
                deathMessage = combatTrackerIn.getDeathMessage().getUnformattedText();
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        eventType = buf.readEnumValue(Event.class);

        if (eventType == S42PacketCombatEvent.Event.END_COMBAT)
        {
            ticksExisted = buf.readVarIntFromBuffer();
            targetEntityID = buf.readInt();
        }
        else if (eventType == S42PacketCombatEvent.Event.ENTITY_DIED)
        {
            attackerEntityID = buf.readVarIntFromBuffer();
            targetEntityID = buf.readInt();
            deathMessage = buf.readStringFromBuffer(32767);
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeEnumValue(eventType);

        if (eventType == S42PacketCombatEvent.Event.END_COMBAT)
        {
            buf.writeVarIntToBuffer(ticksExisted);
            buf.writeInt(targetEntityID);
        }
        else if (eventType == S42PacketCombatEvent.Event.ENTITY_DIED)
        {
            buf.writeVarIntToBuffer(attackerEntityID);
            buf.writeInt(targetEntityID);
            buf.writeString(deathMessage);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleCombatEvent(this);
    }

    public enum Event
    {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DIED
    }
}
