package iuh.fit.backend.Event.configuration;


import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.listener.ExceptionListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.List;


@org.springframework.context.annotation.Configuration
public class SocketIOConfig {
    private static final Logger logger = LoggerFactory.getLogger(SocketIOConfig.class);
    @Value("${socketio.port}")
    private int socketIOPort;

    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setPort(socketIOPort);
        config.setHostname("0.0.0.0");

        // QUAN TRỌNG: Thêm timeout
        config.setUpgradeTimeout(10000);
        config.setPingTimeout(60000);

        // Bắt buộc thêm exception handler
        config.setExceptionListener(new ExceptionListener() {
            @Override
            public void onEventException(Exception e, List<Object> list, SocketIOClient socketIOClient) {

            }

            @Override
            public void onDisconnectException(Exception e, SocketIOClient socketIOClient) {

            }

            @Override
            public void onConnectException(Exception e, SocketIOClient client) {
                System.err.println("CONNECT ERROR: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onPingException(Exception e, SocketIOClient socketIOClient) {

            }

            @Override
            public void onPongException(Exception e, SocketIOClient socketIOClient) {

            }

            @Override
            public boolean exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
                return false;
            }
            // ... implement all methods
        });

        try {
            SocketIOServer server = new SocketIOServer(config);
            server.start(); // QUAN TRỌNG: Phải gọi start() thủ công
            System.out.println("Socket.IO REAL STARTED on port: " + config.getPort());
            return server;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Socket.IO", e);
        }
    }

    private boolean validateToken(String token) {
        // Triển khai logic xác thực token
        return true; // Tạm thời cho phép tất cả
    }
}
