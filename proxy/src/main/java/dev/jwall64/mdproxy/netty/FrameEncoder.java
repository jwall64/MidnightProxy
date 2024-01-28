package dev.jwall64.mdproxy.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public class FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf out) {

        int length = byteBuf.readableBytes();
        PacketBufferUtil.writeVarInt(out, length);
        out.writeBytes(byteBuf);
    }
}
