import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EchoClient {
    private EventLoopGroup group=null;
    private Bootstrap bootstrap=null;

    public EchoClient(){
        init();
    }

    private void init() {
        group=new NioEventLoopGroup();
        bootstrap=new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    public ChannelFuture doRequest(String host,int port, final ChannelHandler... channelHandlers) throws InterruptedException {
        try{
            this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(channelHandlers);
                }
            });
            ChannelFuture future=bootstrap.connect(host,port).sync();
            return future;
        }finally {
            release();
        }
    }

    public void release(){
        this.group.shutdownGracefully();
    }

    public static void main(String[] args) throws InterruptedException {
        EchoClient client=null;
        ChannelFuture future=null;
        try{
            client=new EchoClient();
            future=client.doRequest("127.0.0.1",8080,new EchoClientHandler());
            Scanner scanner=null;
            while (true){
                scanner =new Scanner(System.in);
                System.out.println("enter message send to server (enter 'exit' for close client)");
                String line =scanner.nextLine();
                if ("exit".equalsIgnoreCase(line)){
                    future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes("UTF-8")))
                            .addListener(ChannelFutureListener.CLOSE);
                    break;
                }
                future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes("UTF-8")));
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            if (null!=future){
                try {
                    future.channel().closeFuture().sync();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (null!=client){
                client.release();
            }
        }

    }

}
