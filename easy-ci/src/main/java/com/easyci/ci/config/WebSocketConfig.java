package com.easyci.ci.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Xcloud-Api By IDEA
 * 配置WebSocket消息代理端点，即stomp服务端
 * 为了连接安全，setAllowedOrigins设置的允许连接的源地址
 * 如果在非这个配置的地址下发起连接会报403
 * 进一步还可以使用addInterceptors设置拦截器，来做相关的鉴权操作
 * Created by LaoWang on 2018/8/25.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("*")
                .withSockJS();
    }

//    /**
//     * 推送日志到/topic/pullLogger
//     */
//    public void pushLogger(String name){
//        ExecutorService executorService= Executors.newFixedThreadPool(1);
//        Runnable runnable = new Runnable() {
////        Thread thread=new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        LoggerMessage log = LoggerQueue.getInstance().poll();
//                        if(log!=null){
//                            if(messagingTemplate!=null)
//                            {
//                                while (log.getBody().contains(name)){
//                                    String[] strings = log.getBody().split(name);
//                                    log.setBody(strings[1]);
//                                    messagingTemplate.convertAndSend("/topic/" + name,log);
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
////        thread.start();
//        executorService.submit(runnable);
//    }

}