import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.header.RpcHeader;
import com.lb.rpc.protocol.header.RpcHeaderFactory;
import com.lb.rpc.protocol.request.RpcRequest;

public class Test {
    public static RpcProtocol<RpcRequest> getRpcProtocol() {
        RpcHeader header = RpcHeaderFactory.getRequestHeader("jdk");
        RpcRequest body = new RpcRequest();
        body.setOneway(false);
        body.setAsync(false);
        body.setClassName("com.lb.rpc.demo.RpcProtocol");
        body.setMethodName("hello");
        body.setGroup("zhiyu");
        body.setParameters(new Object[]{"zhiyu"});
        body.setParameterTypes(new Class[]{String.class});
        body.setVersion("1.0.0");
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setBody(body);
        protocol.setHeader(header);
        return protocol;
    }
}
