package com.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;



public class RpcRegistry {
    private int port;
    public RpcRegistry(int port){
        this.port = port;
    }
    public void start(){
        EventLoopGroup bossGroup = new NioEventLoop();
        EventLoopGroup workGroup = new NioEventLoop();
        ChannelFuture future = null;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {// 注意这是childHandler, client用的是handler, 都要重写initChannel方法
                    @Override
                    protected void initChannel(SocketChannel sc)throws Exception{ // 这里是写了一个ChannelInitializer 类的匿名子类，实现了initChannel 方法，当有连接过来时，才会调用这个函数
                        System.out.println("sc:" + sc);
                        ChannelPipeline cp = sc.pipeline();
                        System.out.println("111111");
                        cp.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                        System.out.println("222:");
                        cp.addLast(new LengthFieldPrepender(4));
                        System.out.println("333");
                        cp.addLast("encoder", new ObjectEncoder());
                        System.out.println("444");
                        cp.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        System.out.println("555555");
                        // 这个才是最重要的, 我们处理从channel 中读取出来的数据， 有连接过来时，就会new RegistryHandler,
                        // 这个对象中会从本地搜索所有可用的服务并存到map中。k: interface , v : 实现类的实例
                        // 当server中的netty 的channelRead()函数中收到自定义协议对象后，就会用msg中的interface Name找到 服务器上对应的实现类的实例，然后用这个实例通过反射调用对应的方法，并把结果写到client的ctx中。
                        cp.addLast(new RegistryHandler());
                        System.out.println("6666666");
                    }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)// 来不及处理的连接放在缓存中的数目最大数
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//保持连接
            future = b.bind(port).sync();
            System.out.println("GP registry start listen on port:" + port);
            future.channel().closeFuture().sync();
        }catch (Exception ex){
            ex.printStackTrace();
            if (null != future){
                try {
                    future.channel().closeFuture().sync();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        //发布注册
        new RpcRegistry(8083).start();
    }
}

