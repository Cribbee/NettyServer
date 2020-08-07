package com.netty.rpc.RpcServiceImpl;

import com.netty.rpc.api.IRpcHelloService;

public class RpcHelloServiceImpl implements IRpcHelloService {
    public String hello(String name){
        return "hello: " + name;
    }
}
