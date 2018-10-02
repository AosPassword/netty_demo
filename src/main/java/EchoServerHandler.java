import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in= (ByteBuf) msg;
        String message=in.toString(CharsetUtil.UTF_8);
        System.out.println("server received: "+ message);
        if ("exit".equalsIgnoreCase(message)){
            ctx.close();
            return;
        }
        String line="Server has received";
        ctx.writeAndFlush(Unpooled.copiedBuffer(line.getBytes("UTF-8")));//将接收到的消息写给发送者，不冲刷出站消息
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("server exceptionCaught run");
        cause.printStackTrace();
        ctx.close();
    }
}
