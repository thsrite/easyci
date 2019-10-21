package com.easyci.ci.socket;

import com.easyci.ci.service.DockerLogsService;
import com.easyci.ci.util.SpringUtil;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.InputStream;


@ServerEndpoint("/websocket/{name}")
@RestController
public class WebSocketServer {

    private Process process;

    private InputStream inputStream;

    /**
     * 新的WebSocket请求开启
     */
    @OnOpen
    public void onOpen(@PathParam("name") String name) {
        System.out.println(name);
        String[] message = name.split("\\|");
        DockerLogsService dockerLogsService = (DockerLogsService) SpringUtil.getBean("dockerLogsService");
        if (message.length == 3){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dockerLogsService.dockerLogs(message[0],message[2],message[1]);
                }
            }).start();

        }
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
        if(inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e) {
                System.out.println("close websocket error.:" + e);
            }
        }

        if(process != null){
            process.destroy();
        }
    }

    @OnError
    public void onError(Throwable thr) {
        System.out.println("websocket error." + thr);
    }
}