# NettyServer
说明：

1.RPC 调用是在client只知道接口和方法名的情况要，通过发送给server 由server调用后返回结果的。所以我们要创建一个服务接口。
==本示例中有两个接口：IRpcHelloService, 和IRpcService， 放在包api中。

2. 有了服务接口，那就必须要有实现类，这个在实际中一定是放到server上的。
==本示例实现类RpcHelloServiceImpl, 和RpcServiceImpl, 放在包RpcServiceImpl中。

3. 服务器要提供服务，那就要把所有的可用服务显露出来。
==本文中由类RpcRegistry 类中的start() 通过Port 绑定在netty上来向网络发布服务。
==netty中必然要用到childHandler的配置，这里面就要用到我们自定义的handler （RegistryHandler类）,  重写channelRead()方法

4.client端要请求服务,我们定义一个consumer类.在这个类中因为client只知道 接口名,我们需要为client端创建动态代理对象,通过代理对象来执行对应的方法.代理对象中invoke方法通过netty 向server端发送自定义的协议. 里边也会有自己定义的handler, 里面也要重写channelRead() 来处理收到的报文.

5. 在上一步骤中会有到自定义的协议,所以我们也要定义一个InvokerProtocol, 里面肯定会接口名,方法名,方法参数.
服务器上会根据收到的接口名,找到可用的服务实现类的实例.
