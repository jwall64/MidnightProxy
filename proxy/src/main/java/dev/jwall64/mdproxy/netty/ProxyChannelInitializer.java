package dev.jwall64.mdproxy.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import dev.jwall64.mdproxy.ClientPacketHandler;
import dev.jwall64.mdproxy.ProxyServer;
import dev.jwall64.mdproxy.packet.PacketRegistry;

import java.util.concurrent.TimeUnit;

public class ProxyChannelInitializer extends ChannelInitializer<Channel> {


    private final ProxyServer server;

    public ProxyChannelInitializer(ProxyServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {

        channel.pipeline()
                .addLast(new ReadTimeoutHandler(server.getClientTimeout(), TimeUnit.MILLISECONDS))
                .addLast("frame_enc", new FrameEncoder())
                .addLast("encoder", new PacketEncoder())
                .addLast("frame_dec", new FrameDecoder())
                .addLast("decoder", new PacketDecoder(PacketRegistry.HANDSHAKE))
                .addLast("handler", new ClientPacketHandler(channel, server));
    }
}
