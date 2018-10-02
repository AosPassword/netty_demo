import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {
    private EventLoopGroup acceptGroup=null;
    private EventLoopGroup clientGroup=null;
    private ServerBootstrap b=null;

    public EchoServer(){
        init();
    }

    private void init() {
        acceptGroup=new NioEventLoopGroup();
        clientGroup=new NioEventLoopGroup();
        b=new ServerBootstrap();
        b.childOption(ChannelOption.SO_KEEPALIVE,true);
        b.group(acceptGroup,clientGroup)
                .channel(NioServerSocketChannel.class);
    }


    public ChannelFuture accept(int port,final ChannelHandler... echoServerHandlers) throws InterruptedException {
        ChannelFuture future=null;
        try{
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(echoServerHandlers);
                }
            });
            future=b.bind(port).sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            release();
        }
        return future;
    }
    public void release(){
        this.acceptGroup.shutdownGracefully();
        this.clientGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        ChannelFuture future=null;
        EchoServer echoServer=null;
        try{
            echoServer = new EchoServer();
            future=echoServer.accept(8080,new EchoServerHandler());
            System.out.println("server started");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (null!=future){
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (null!=echoServer){
                echoServer.release();
            }
        }
    }
}