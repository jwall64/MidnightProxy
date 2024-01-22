package dev.jwall64.mdproxy.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private static final Logger LOGGER = LoggerFactory.getLogger("PacketEncoder");

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {

        LOGGER.warn("Sending packet " + packet.getClass().getName() + "!");

        PacketBufferUtil.writeVarInt(byteBuf, packet.getId());
        byteBuf.release();
        packet.write(byteBuf);

    }
}
