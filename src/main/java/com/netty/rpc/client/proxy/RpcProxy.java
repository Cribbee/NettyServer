package com.netty.rpc.client.proxy;

import com.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy {
    //封装一下newProxyInstance
    public static <T> T createProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MethodProxy(clazz));
    }

    //动态代理的Handler
    private static class MethodProxy implements InvocationHandler{
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz){
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)throws Throwable{
            if (Object.class.equals(method.getDeclaringClass())){
                return method.invoke(this, args);
            }
            else {
                // invoke rpc server to execute method
                return rpcInvoke(proxy, method, args);
            }
        }

        // 远程调用 部分用netty来实现
        public Object rpcInvoke(Object proxy, Method method, Object[] args){
            //Package the msg and send to server
            //构造自定义协议报文
            InvokerProtocol myMsg = new InvokerProtocol();
            myMsg.setClssName(this.clazz.getName());
            myMsg.setMethodName(method.getName());
            myMsg.setParaTypes(method.getParameterTypes());
            myMsg.setValues(args);

            //netty 中不可少的handler, 继承自ChannelInboundHandlerAdaptor, 重写channelRead() exceptionCaught()
            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            // 创建工作组
            EventLoopGroup workGroup = new NioEventLoopGroup();
            try {
                // bootStrap 不可少，server端用的是ServerBootStrap
                Bootstrap b = new Bootstrap();
                b.group(workGroup)
                        .channel(NioSocketChannel.class)// server用的是NioServerSocketChannel
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            // handler 设置不可少, pipeline 加handler. server用的是childHandler()
                            @Override
                            protected  void initChannel(SocketChannel ch) throws Exception{
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,4, 0,4));
                                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                pipeline.addLast("encoder", new ObjectEncoder());
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast("handler",  consumerHandler); // 最后添加我们自己的handler
                            }
                        });
                try {
                    // 连接
                    ChannelFuture future = b.connect("localhost", 8083).sync();
                    System.out.println("write: " + myMsg);
                    // 写报文
                    future.channel().writeAndFlush(myMsg);
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }finally {
                workGroup.shutdownGracefully();
            }
            // 把server 返回的结果，返回给client调用的地方
            return consumerHandler.getResponse();
        }
    }
}
