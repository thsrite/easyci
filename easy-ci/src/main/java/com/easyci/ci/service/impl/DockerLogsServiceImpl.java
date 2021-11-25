package com.easyci.ci.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.easyci.ci.config.EasyCiConfig;
import com.easyci.ci.dao.DockerLogsMapper;
import com.easyci.ci.dao.SequenceMapper;
import com.easyci.ci.entity.DockerLogs;
import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.DockerLogsService;
import com.easyci.ci.service.DockerServerService;
import com.easyci.ci.util.ConnectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("dockerLogsService")
public class DockerLogsServiceImpl implements DockerLogsService {

    @Autowired
    private SequenceMapper sequenceMapper;
    @Autowired
    private EasyCiConfig easyCiConfig;
    @Autowired
    private DockerLogsMapper dockerLogsMapper;
    @Autowired
    private DockerServerService dockerServerService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public ResponseResult catchServerLog(String giturl, Integer start, DockerServer dockerServer) {
        String container_name = "";
        Pattern pattern = Pattern.compile("/(.*?).git");
        Matcher matcher = pattern.matcher(giturl);
        while (matcher.find()) {
            if (giturl.startsWith("git")) {
                container_name = matcher.group(1);
            } else if (giturl.startsWith("http")) {
                String[] strings = matcher.group(1).split("/");
                container_name = strings[strings.length - 1];
            } else {
                return new ResponseResult(false, 400, "请输入正确的giturl地址", null);
            }
        }
        Process ps = null;
        try {
            String sequenceName = container_name + ":" + dockerServer.getServer_ip();
            Integer deploynum = sequenceMapper.selectCurrentVal(sequenceName);
            String logs_name = container_name + "_" + dockerServer.getServer_ip() + "_" + deploynum;
            String logs_Path = this.easyCiConfig.getLogpath() + "/" + container_name + "/" + logs_name;
            String line = "";

            String[] cmd = new String[]{"sh", "-c", "cat " + logs_Path};
            ps = Runtime.getRuntime().exec(cmd);
            int exitValue = ps.waitFor();
            //当返回值为0时表示执行成功
            if (0 != exitValue) {
                Map<String, Object> map = new HashMap<>();
                map.put("start", 1);
                map.put("log", "…");
                return new ResponseResult(false, 500, "获取日志中…", map);
            }
            InputStream is = new StreamGobbler(ps.getInputStream());
            BufferedReader brs = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            int end = 1;
            long realSkip = brs.skip(start);
            while ((line = brs.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
                /* 将读取的长度追加到变量中 */
                realSkip += sb.length();
                if ("容器远程回滚成功".equals(line) || "远程端口已被占用".equals(line) ||
                        "容器远程部署成功".equals(line) || "容器远程部署失败".equals(line) ||
                        "远程服务器连接失败".equals(line) || "镜像拉取失败".equals(line) ||
                        "本地端口已被占用".equals(line) || "系统错误".equals(line)) {
                    end = 0;
                }
            }
            brs.close();
            is.close();
            ps.destroy();
            Map<String, Object> map = new HashMap<>();
            map.put("start", realSkip);
            map.put("log", sb);
            map.put("endsign", end);
            return new ResponseResult(true, 200, "日志", map);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(false, 500, "系统错误", e);
        }
    }

    @Override
    public ResponseResult dockerLogs(String container_name, String server_ip, String random) {
        Connection con = null;
        DockerServer dockerServer = new DockerServer();
        dockerServer.setServer_ip(server_ip);
        DockerServer dockerServer1 = dockerServerService.selectByDockerServer(dockerServer);
        con = ConnectUtil.getConnect(dockerServer1.getServer_ip(), dockerServer1.getServer_username(), dockerServer1.getServer_password(), Integer.parseInt(dockerServer1.getServer_port()));
        if (con != null) {
            Session ss = null;
            try {
                String line = "";
                ss = con.openSession();
                String cmd = "docker logs -f --tail 200 " + container_name;
                ss.execCommand(cmd);
                InputStream is = new StreamGobbler(ss.getStdout());
                BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                while ((line = brs.readLine()) != null) {
                    if (messagingTemplate != null) {
                        Thread.sleep(1);
                        messagingTemplate.convertAndSend("/topic/" + container_name + random, line);
                    }
                }
                brs.close();
                is.close();
                ss.close();
                con.close();
                return new ResponseResult(true, 200, "日志", null);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseResult(false, 500, "系统错误", e);
            }
        } else {
            return new ResponseResult(false, 400, "连接服务器失败", null);
        }
    }

    @Override
    public ResponseResult killDockerLogs(String container_name, String server_ip) {
        Connection con = null;
        DockerServer dockerServer = new DockerServer();
        dockerServer.setServer_ip(server_ip);
        DockerServer dockerServer1 = dockerServerService.selectByDockerServer(dockerServer);
        con = ConnectUtil.getConnect(dockerServer1.getServer_ip(), dockerServer1.getServer_username(), dockerServer1.getServer_password(), Integer.parseInt(dockerServer1.getServer_port()));
        if (con != null) {
            Session ss = null;
            try {
                ss = con.openSession();
                String cmd = "ps -ef | grep docker | grep " + container_name + " | awk '{print $2}' | xargs kill -9";
                ss.execCommand(cmd);
                ss.close();
                con.close();
                return new ResponseResult(true, 200, "实时日志关闭成功", null);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseResult(false, 500, "系统错误", e);
            }
        } else {
            return new ResponseResult(false, 400, "连接服务器失败", null);
        }
    }

    @Override
    public ResponseResult rmBuildLog(String container_name, String deploy_ip) {
        List<DockerLogs> dockerLogs = dockerLogsMapper.selectBy(container_name, deploy_ip);
        if (dockerLogs.size() > 0) {
            for (DockerLogs dockerLogs1 : dockerLogs) {
                String[] cmd = new String[]{"sh", "-c", "rm -rf " + dockerLogs1.getLogs_path()};
                System.out.println(Arrays.toString(cmd));
                Process ps = null;
                try {
                    ps = Runtime.getRuntime().exec(cmd);
                    int exitValue = ps.waitFor();
                    if (0 != exitValue) {
                        return new ResponseResult(false, 500, "删除构建日志失败", null);
                    }
                    ps.destroy();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    return new ResponseResult(false, 500, "系统错误", e);
                }
            }
            return new ResponseResult(true, 200, "删除构建日志成功", null);
        } else {
            return new ResponseResult(true, 200, "无需删除构建日志", null);
        }
    }
}
