package dev.jwall64.mdproxy.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketRegistry;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.UnknownPacket;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import java.util.List;

public class PacketDecoder<T> extends ByteToMessageDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger("PacketDecoder");
    private PacketRegistry<T> registry;

    public PacketDecoder(PacketRegistry<T> registry) {
        this.registry = registry;
    }

    public void setRegistry(PacketRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytes, List<Object> out) throws Exception {

        int id = PacketBufferUtil.readVarInt(bytes);

        Packet<T> p = registry.read(id, bytes);
        if(p == null) {
            LOGGER.warn("Found unknown packet with id " + id);
            out.add(new UnknownPacket(id, bytes));
            return;
        }

        out.add(p);
    }


}
