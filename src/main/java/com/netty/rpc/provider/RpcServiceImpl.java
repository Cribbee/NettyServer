package com.netty.rpc.provider;

import com.netty.rpc.api.IRpcService;

public class RpcServiceImpl implements IRpcService {
    public int add(int a, int b){
        return a + b;
    }

    public int sub(int a, int b){
        return a - b;
    }
}
