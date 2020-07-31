package com.netty.rpc.registry;

import com.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// handler 都是继承这个类ChannelInboundHandlerAdapter， 并重写channelRead 和exceptionCaught（）两个方法
public class RegistryHandler extends ChannelInboundHandlerAdapter {
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();
    private List<String> classNames = new ArrayList<String>();

    // RegistryHandler是用来处理client发来的请求的，所以他必须要知道哪些服务可用，所在在构造函数里，这把所有要用的服务初始化好。
    public RegistryHandler() {
        doScan("com.netty.rpc.provider");
        doRegistry();
    }
    public void doScan(String packagePath) {
        // 通过把 xx.xx.xx 替换成/xx/xx/xx/xx
        URL url = this.getClass().getClassLoader().getResource(packagePath.replaceAll("\\.", "/"));
        System.out.println("url:" + url);
        // /xx/xx/xx 变成 e:\xx\xx\xx
        File dir = new File(url.getFile());
        System.out.println("dir:" + dir);
        for (File f : dir.listFiles()) {
            if (f.isDirectory()){
                //如果是目录，就递归调用
                doScan(packagePath + "." + f.getName());
            }else {
                // 如果是.class文件，就把class的全名放到list中
                classNames.add(packagePath + "." + f.getName().replace(".class","").trim());
            }
        }
    }
    public void doRegistry(){
        if (classNames.size() == 0)
            return;

        try {
            // 把上边可用的所有服务（服务的实现类），全部映射成 k: 未实现的接口，v: 实现类的实例 的map 存起来
            for (String className : classNames){
                System.out.println("className:" + className);
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(), clazz.newInstance());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    // channel.pipeline.addLast(handler中)， 加了这个handler, 那就会自动调用 channelRead()方法，这里就是最核心的调用地方
    public void channelRead(ChannelHandlerContext ctx, Object msg)throws  Exception{
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;
        System.out.println("request: " + request);
        //从收到的msg中提取出接口名，方法名，方法形参，方法实参，然后找到对应的实现类的实例，用反射调用方法
        if (registryMap.containsKey(request.getClssName())){
            Object clazzInstance = registryMap.get(request.getClssName());
            Method method = clazzInstance.getClass().getMethod(request.getMethodName(), request.getParaTypes());
            //调用函数的结果??
            result = method.invoke(clazzInstance, request.getValues());
            System.out.println("result: " + result);
            // 将反射执行后的结果写回给client
            ctx.writeAndFlush(result);
            ctx.flush();
            ctx.close();
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception{
        super.exceptionCaught(ctx,cause);
        ctx.close();
    }
}
