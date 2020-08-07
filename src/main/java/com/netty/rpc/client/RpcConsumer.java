package com.netty.rpc.client;

import com.netty.rpc.api.IRpcHelloService;
import com.netty.rpc.api.IRpcService;
import com.netty.rpc.client.proxy.RpcProxy;

public class RpcConsumer {
    public static void main(String[] args) {
        // client 要调用远程的服务，他本身只知道接口名，所以他本身如何让server知道 自己的需要呢
        // 就要把接口名和方法名传给server。
        // 但是在本地呢，他还是调用原来的方法名。
        // 还有就是在本地只有接口，不能实例化，那我们就只能给他创建一个代理对象
        // 根据动态代理的原理，我们先要创建一个代理类，实现InvocationHandler, 并重写invoke方法
        // 在invoke方法里，把我们自定义的协议对象发给server, 然后再读取server的返回，然后再把这个返回值返回给本地调用的方法
        IRpcHelloService rpcHello = RpcProxy.createProxy(IRpcHelloService.class);
        System.out.println("out:" + rpcHello.hello("lxy"));

        IRpcService service = RpcProxy.createProxy(IRpcService.class);
        int r = service.add(1,1);
        System.out.println("result 1 + 1 =" + r);
    }
}
