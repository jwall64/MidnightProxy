package dev.jwall64.mdproxy.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {

        PacketBufferUtil.writeVarInt(byteBuf, packet.getType().getId());
        packet.write(byteBuf);

    }
}
