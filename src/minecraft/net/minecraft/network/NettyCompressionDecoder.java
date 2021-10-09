package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;
import java.util.zip.Inflater;

public class NettyCompressionDecoder extends ByteToMessageDecoder
{
    private final Inflater inflater;
    private int threshold;

    public NettyCompressionDecoder(int threshold)
    {
        this.threshold = threshold;
        inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> output) throws Exception
    {
        if (buffer.readableBytes() != 0)
        {
            PacketBuffer packetbuffer = new PacketBuffer(buffer);
            int i = packetbuffer.readVarIntFromBuffer();

            if (i == 0)
            {
                output.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            }
            else
            {
                if (i < threshold)
                {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + threshold);
                }

                if (i > 2097152)
                {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + 2097152);
                }

                byte[] abyte = new byte[packetbuffer.readableBytes()];
                packetbuffer.readBytes(abyte);
                inflater.setInput(abyte);
                byte[] abyte1 = new byte[i];
                inflater.inflate(abyte1);
                output.add(Unpooled.wrappedBuffer(abyte1));
                inflater.reset();
            }
        }
    }

    public void setCompressionThreshold(int threshold)
    {
        this.threshold = threshold;
    }
}
