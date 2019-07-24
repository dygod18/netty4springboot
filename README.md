# netty4springboot
基于netty做为网络层框架的spring boot项目 demo

### 概要
        
#### 怎么替换掉spring boot 内嵌的tomcat ？ 
    - 首先了解什么是servlet 容器
        - 什么是Servlet？
            - 事实上，servlet就是一个Java接口，interface! 
            - 那servlet是干嘛的？很简单，接口的作用是什么？规范呗！
            > servlet接口定义的是一套处理网络请求的规范，所有实现servlet的类，都需要实现它那五个方法，其中最主要的是两个生命周期方法 init()和destroy()，还有一个处理请求的service()，
            servlet是一个规范，那实现了servlet的类，就能处理请求了吗？
            你可以随便谷歌一个servlet的hello world教程，里面都会让你写一个servlet，相信我，你从来不会在servlet中写什么监听8080端口的代码，servlet不会直接和客户端打交道！那请求怎么来到servlet呢？答案是servlet容器，比如我们最常用的tomcat，同样，你可以随便谷歌一个servlet的hello world教程，里面肯定会让你把servlet部署到一个容器中，不然你的servlet压根不会起作用。tomcat才是与客户端直接打交道的家伙，他监听了端口，请求过来后，根据url等信息，确定要将请求交给哪个servlet去处理，然后调用那个servlet的service方法，service方法返回一个response对象，tomcat再把这个response返回给客户端。

        - 所以我们在定义一个servlet 容器时，需要做什么呢？
            - 首先初始化容器 init方法，由context init
            - 其次接受请求处理请求。service 方法
            - 然后能关闭，销毁，destroy

        - servlet 规范的核心接口
            - ServletContext：定义了一些可以和Servlet Container交互的方法。
            - Registration：实现Filter和Servlet的动态注册。
            - ServletRequest(HttpServletRequest)：对HTTP请求消息的封装。
            - ServletResponse(HttpServletResponse)：对HTTP响应消息的封装。
            - RequestDispatcher：将当前请求分发给另一个URL，甚至ServletContext以实现进一步的处理。
            - Servlet(HttpServlet)：所有“服务器小程序”要实现了接口，这些“服务器小程序”重写doGet、doPost、doPut、doHead、doDelete、doOption、doTrace等方法(HttpServlet)以实现响应请求的相关逻辑。
            - Filter(FilterChain)：在进入Servlet前以及出Servlet以后添加一些用户自定义的逻辑，以实现一些横切面相关的功能

        - 简单看下spring boot 中servlet的处理流程
            - GenericServlet
            > servlet 规范的一部分，并不直接关注HTTP，提供了service 方法，接受request，生成reponse。
            - HttpServlet
            > 顾名思义，HttpServlet类就是规范中定义的基于HTTP的Servlet实现。用更实际的术语来说，Httpservlet 是一个实现了service
            方法的抽象类。基于http的请求类型分割方法
            - HttpServletBean
            > 接下来，HttpServletBean是层次结构中第一个支持Spring的类。 它使用从web.xml或WebApplicationInitializer收到的servlet 
            init-param值来注入bean的属性。
            - FrameworkServlet
            > FrameworkServlet将Servlet功能与Web应用程序上下文集成，实现ApplicationContextAware接口。
            但它也能够自行创建Web应用程序上下文。
            - DispatcherServlet
            > HttpServlet.service 方法的实现类，基于http verb 的类型来路由到不同的controller

    - 再看看embed tomcat 做了什么事
        - 先介绍几个关键的组件
            - Endpoint 用来处理底层的socket 网络连接， 即用来实现Tcp/ip 协议
                - Acceptor 用于监听tcp连接，并将对应的根据状态注册到poller 上
                - Poller 将socket 添加到poller 队列， 并且轮询pollers events 队列，将关联的套接字交给适当的处理器处理
                - SocketProcessor 等同于一个Worker，用于从socket中读取数据，最后丢给业务代码处理。
            - Processor 用于将Endpoint接受到的socket 封装成request， 即用来实现http 协议
            - Adapter 用于将封装好的request 交给Container 处理，即将请求适配到Servlet 容器进行处理

        - 一个浏览器请求进来的处理过程：
            0、通过mian 函数，启动内置的servlet 容器，即内置tomcat。
            1、启动Acceptor 监听tcp连接，有tcp连接时，将socket 注册到Poller 的events 队列里 
            2、Poller 会轮询 events 队列，当有pollerEvent时，运行pollEvent 的run方法注册一个OP_READ 的事件。（在selector 上注册标记位，标示可读、可写或者有新的连接到来）
            3、2、Poller.processKey 会处理被标记为OP_READ 或 OP_WRITE的事件，基于给定的状态做不同的处理。实际就是新起一个SocketProcessor处理请求。
            4、NioEndpoint$SocketProcessor，等同于一个Worker，用于从socket中读取数据，最后丢给业务代码处理。
            ````
            Nio2Endpoint$Socketprocessor.doRun  //处理socket read事件，并基于处理完成返回的state处理socket
                - AbstractProtocol$ConnectionHandler.process //根据协议选择对应的processor，并基于返回的socket状态判断处理processor
                    - AbstractProcessorLight.process
                        - Http11ProcessorLight.service    //实际的协议处理类，会调用CoyoteAdapter.service拿到reponse，并基于返回值设置response code 
                            - CoyoteAdapter.service
                                - StrandardEngineValve.invoke
                                - ErrorReportValve.invoke
                                - StrandardHostValve.invoke
                                - AuthenticatorBase.invoke
                                - StrandardContextValve.invoke
                                - StrandardWrapperValve.invoke  // 创建一个ApplicationFilterChain 并向其中注入servlet (dispatcherServlet)
                                    - ApplicationFilterChain.doFilter   //Application FilterChain 首先轮询FilterChain 上面的Filter，执行doFilter 方法，然后调用DispatcherServlet.service 交给spring 框架执行接下来的处理逻辑
                                    .
                                    .
                                    .
                                    .
                                        - HttpServlet.service
                                            - FrameworkServlet.service
                                            - FrameworkServlet.doService
                                            - FrameworkServlet.processRequest
                                                - DispatcherServlet.doService
                                                    - ServletServerHttpResponse // 封装了HttpServletResponse，通过AbstractHttpMessageConverter 写入到servletResponse 的outputStream中，
                                                        然后调用flush 方法刷到客户端
            ````
            5、当业务代码处理完以后，获取servletResponse的outputstream 将数据写入到buffer 中。
            6、调用Outputstream 的 flush 方法将数据刷新到socket 中

#### 基于netty 替换embed tomcat 网络层 如何做呢？
    - 首先看看netty 能干什么
        - 1、先看看通常我们基于netty写一个http 服务器时，是怎么做的：
            - Netty 服务端时序图.png
            - 1、创建两个NioEventLoopGroup 实例，一个用于服务端接收客户端的连接，一个用于进行SocketChannel 的网络读写
            - 2、创建 ServerBootstrap 对象， 它是Netty用于启动NIO服务端的辅助启动类
            - 3、调用ServerBootstrap 的group方法，将两个NIO线程组当作入参传递到ServerBootStrap 中，接着设置创建的Channel 为 NioServerSocketChannel，
            它的功能对应于JDK NIO类库中的ServerSocketChannel类。然后配置NioServerSocketChannel 的TCP参数，同时将他的backlog 设置为1034（即Accept queue），
            最后绑定ChildChannelHandler用于处理I／O事件
            - 4、调用 ServerBootstrap 的bind 方法绑定监听端口，随后，调用它的同步阻塞方法，sync 等待绑定操作完成。完成之后Netty 会返回一个ChannelFuture，
            它的功能类似于JDK 的 java.util.concurrent.Future，主要用于异步操作的通知回调
            - 5、使用f.channel().closeFuture().sync() 方法进行阻塞， 等到服务端链路关闭之后main 函数才退出
            - 6、调用NIO 线程组的shutdownGracefully 进行优雅退出，他会释放genshutdownGracefully 相关联的资源
    
    - 几个关键组件
        - EmbeddedNettyFactory  // 核心类 ，实现自spring 的 EmbeddedServletContainerFactory，用于生产                 EmbeddedServletContainer， 同时调用ServletContextInitializer 的onstartup 对ServletContext进行初始化配置
        - NettyContiner  // 用于启动netty 容器 
            - HttpServerCodec   // HttpRequestDecoder 和 HttpResponseEncoder 的组合，实际就是字节数组 转化为 HttpMessage
            - ServletContentHandler // 将HttpMessage 封装为 HttpServletRequest 和 HttpServletResponse 方便交给spring 框架做处理
            - RequestDispatcherHandler  // 将封装好的HttpServletRequest 和 HttpServletResponse 分发给对应的servlet 处理，实际就是dispatcherServlet
        - NettyServletRegistration // 继承自ServletRegistration.Dynamic 用于向context 中注册 servlet mapping，以及动态注入Servlet
        - SimpleFilterChain //实现自FilterChain，引用了servlet，在做完filter 逻辑后调用servlet.service 
        - NettyServletRequest   // 实现HttpServletRequest接口，引入ServletInputStream，servletContext 方便用于对输入数据做一些额外处理
        - NettyServletResponse  // 实现HttpServletRequest接口，引入ServletOutputStream， 主要用于回写业务处理的结果到ServletOutputStream 中
        - NettyResponseOutputStream  // 继承自ServletOutputStream，引入了ChannelHandlerContext，用语将结果会写到ByteBuf 中
        - NettyRequestInputStream   // 继承自ServletInputStream，方便对输入流做额外处理
