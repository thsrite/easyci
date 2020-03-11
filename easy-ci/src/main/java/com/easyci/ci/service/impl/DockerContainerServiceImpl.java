package com.easyci.ci.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.easyci.ci.api.GitlabAPI;
import com.easyci.ci.api.TokenType;
import com.easyci.ci.api.models.GitlabProject;
import com.easyci.ci.api.models.GitlabProjectHook;
import com.easyci.ci.config.EasyCiConfig;
import com.easyci.ci.config.ServerConfig;
import com.easyci.ci.dao.ContainerDeployMapper;
import com.easyci.ci.dao.DockerLogsMapper;
import com.easyci.ci.dao.GitlabTokenMapper;
import com.easyci.ci.dao.SequenceMapper;
import com.easyci.ci.entity.*;
import com.easyci.ci.service.DockerContainerService;
import com.easyci.ci.service.DockerLogsService;
import com.easyci.ci.service.DockerServerService;
import com.easyci.ci.util.ConnectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("dockerContainerService")
public class DockerContainerServiceImpl implements DockerContainerService {

    @Autowired
    private DockerServerService dockerServerService;
    @Autowired
    private EasyCiConfig easyCiConfig;
    @Autowired
    private SequenceMapper sequenceMapper;
    @Autowired
    private DockerLogsMapper dockerLogsMapper;
    @Autowired
    private ContainerDeployMapper containerDeployMapper;
    @Autowired
    private DockerLogsService dockerLogsService;
    @Autowired
    private GitlabTokenMapper gitlabTokenMapper;
    @Autowired
    private ServerConfig serverConfig;

    @Override
    public List<DockerContainer> getDockerPs(String hostName, String username, String password, Integer port) {
        Connection con = null;
        if (hostName == null || "".equals(hostName)){
            return null;
        }else {
            try {
                con = ConnectUtil.getConnect(hostName, username, password, port);
                List<DockerContainer> list = new ArrayList<>();
                if (con != null) {
                    Session ss = null;
                    try {
                        ss = con.openSession();
                        ss.execCommand("docker ps -a");
                        InputStream is = new StreamGobbler(ss.getStdout());
                        BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                        String line = "";
                        int i = 0;
                        while ((line = brs.readLine()) != null) {
                            String[] splits = line.trim().split("\\s+\\s+");
                            if (splits.length > 0) {
                                String[] status = splits[4].split(" ");
                                String deployStatus = splits[4];
                                if (status.length > 0){
                                    deployStatus = status[0];
                                }
                                if (splits.length == 6) {
                                    list.add(new DockerContainer(splits[0], splits[1], splits[2], splits[3], deployStatus, null, splits[5], hostName));
                                } else {
                                    list.add(new DockerContainer(splits[0], splits[1], splits[2], splits[3], deployStatus, splits[5], splits[6], hostName));
                                }
                            }
                            i++;
                        }
                        brs.close();
                        is.close();
                        ss.close();
                        con.close();
                        list.remove(0);
                        return list;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

    @Override
    public ResponseResult getAllDockerPs() {
        List<DockerServer> servers = dockerServerService.selectServers();
        if (servers != null && servers.size() > 0){
            List<DockerContainer> dockerContainerList = new ArrayList<>();
            for(DockerServer dockerServer: servers){
                List<DockerContainer> dockerContainers = getDockerPs(dockerServer.getServer_ip(),dockerServer.getServer_username(),dockerServer.getServer_password(), Integer.valueOf(dockerServer.getServer_port()));
                if (dockerContainers != null && dockerContainers.size() > 0){
                    dockerContainerList.addAll(dockerContainers);
                }
            }
            List<DockerContainer> stopDockerContainers = new ArrayList<>();
            List<DockerContainer> runDockerContainers = new ArrayList<>();
            List<DockerContainer> allDockerContainers = new ArrayList<>();
            if (dockerContainerList.size() > 0){
                for (DockerContainer dockerContainer:dockerContainerList){
                    if ("Up".equals(dockerContainer.getStatus())){
                        runDockerContainers.add(dockerContainer);
                    }else {
                        stopDockerContainers.add(dockerContainer);
                    }
                }
                allDockerContainers.addAll(stopDockerContainers);
                allDockerContainers.addAll(runDockerContainers);
                return new ResponseResult(true,200,"初始化容器列表成功",allDockerContainers);
            }else {
                return new ResponseResult(false,500,"初始化容器列表失败",null);
            }
        }else {
            return new ResponseResult(false,500,"请添加服务器",null);
        }
    }

    @Override
    public List<DockerContainer> localDockerPs() {
        Process process = null;
        String[] cmd = new String[]{"sh","-c","docker ps -a"};
        List<DockerContainer> dockerContainers = new ArrayList<>();
        try {
            process = Runtime.getRuntime().exec(cmd);
            InputStream is = new StreamGobbler(process.getInputStream());
            BufferedReader brs = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int i = 0;
            while ((line = brs.readLine()) != null) {
                String[] splits = line.trim().split("\\s+\\s+");
                if (splits.length > 0) {
                    String[] status = splits[4].split(" ");
                    String deployStatus = splits[4];
                    if (status.length > 0){
                        deployStatus = status[0];
                    }
                    if (splits.length == 6) {
                        dockerContainers.add(new DockerContainer(splits[0], splits[1], splits[2], splits[3], deployStatus, null, splits[5], "本机"));
                    } else {
                        dockerContainers.add(new DockerContainer(splits[0], splits[1], splits[2], splits[3], deployStatus, splits[5], splits[6], "本机"));
                    }
                }
                i++;
            }
            brs.close();
            is.close();
            process.destroy();
            dockerContainers.remove(0);
            return dockerContainers;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseResult easyci(String giturl, String ports, String language, String mails, String deploy_way, String buildOrLogs,Integer start) {
        //如果是git url，转换为http url
        if ("git".equals(giturl.substring(0,3))) {
            GitlabToken gitlabToken = gitlabTokenMapper.select();
            if (gitlabToken != null){
                GitlabAPI gitlabAPI = GitlabAPI.connect(gitlabToken.getGiturl(),gitlabToken.getAccess_token(), TokenType.ACCESS_TOKEN);
                List<GitlabProject> gitlabProjects = null;
                try {
                    gitlabProjects = gitlabAPI.getProjects();
                    for (GitlabProject gitlabProject:gitlabProjects){
                        if (gitlabProject.getSshUrl().equals(giturl)){
                            giturl = gitlabProject.getHttpUrl();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                return new ResponseResult(false,500,"请设置gitlab验证",null);
            }
        }

        DockerServer dockerServer = new DockerServer();
        dockerServer.setServer_ip(deploy_way);
        DockerServer dockerServer1 = dockerServerService.selectByDockerServer(dockerServer);
        if (dockerServer1 != null){
            if ("build".equals(buildOrLogs)){
                return build(giturl,ports,language,mails,dockerServer1);
            } else if ("logs".equals(buildOrLogs)){
                return dockerLogsService.catchServerLog(giturl,start,dockerServer1);
            } else {
                return null;
            }
        }else {
            return new ResponseResult(false,500,"请选择部署服务器",null);
        }
    }

    @Override
    @Transactional
    public ResponseResult build(String giturl, String ports, String language, String mails, DockerServer dockerServer) {
        List<DockerContainer> dockerContainers = localDockerPs();
        if (dockerContainers.size() == 0){
            return new ResponseResult(false,500,"本机docker私服容器不存在，请检查",null);
        }else {
            for (DockerContainer dockerContainer:dockerContainers){
                if ("qdockerhub".equals(dockerContainer.getContainer_name()) && ! "Up".equals(dockerContainer.getStatus())){
                    return new ResponseResult(false,500,"本机docker私服容器状态不正常，请检查",null);
                }
            }
        }
        String container_name = "";
        Pattern pattern = Pattern.compile("/(.*?).git");
        Matcher matcher = pattern.matcher(giturl);
        while (matcher.find()){
            if ("git".equals(giturl.substring(0,3))){
                container_name = matcher.group(1);
            }else if ("htt".equals(giturl.substring(0,3))){
                String[] strings = matcher.group(1).split("/");
                container_name = strings[strings.length - 1];
            }else {
                return new ResponseResult(false,400,"请输入正确的giturl地址",null);
            }
        }

        GitlabToken gitlabToken1 = gitlabTokenMapper.select();
        if (gitlabToken1 != null){
            if (giturl.length() > 3 && "htt".equals(giturl.substring(0,3))){
                String[] git = giturl.split("//");
                giturl = git[0] + "//" + gitlabToken1.getUsername() + ":" + gitlabToken1.getPassword() + "@" + git[1];
            }
        }else{
            return new ResponseResult(false,500,"请设置gitlab验证",null);
        }

        Process ps = null;
        try {
            String dockerHub = this.serverConfig.getIpAdd() + ":5000";
            String logs_name = "";
            String sequenceName = container_name + ":" + dockerServer.getServer_ip();
            if (sequenceMapper.selectByPrimaryKey(sequenceName) == null){
                //首次部署
                //设置gitlab webhook
                GitlabToken gitlabToken = gitlabTokenMapper.select();
                if (gitlabToken != null){
                    GitlabAPI gitlabAPI = GitlabAPI.connect(gitlabToken.getGiturl(),gitlabToken.getAccess_token(), TokenType.ACCESS_TOKEN);
                    List<GitlabProject> gitlabProjects = gitlabAPI.getProjects();
                    for (GitlabProject gitlabProject:gitlabProjects){
                        if (gitlabProject.getName().toLowerCase().equals(container_name)){
                            String webhook = "http://" + this.serverConfig.getIpAdd() + ":" + this.serverConfig.getPort() + "/gitlab/hook";
                            List<GitlabProjectHook> gitlabProjectHooks = gitlabAPI.getProjectHooks(gitlabProject.getId());
                            if (gitlabProjectHooks.size() > 0){
                                for (GitlabProjectHook gitlabProjectHook:gitlabProjectHooks){
                                    if (gitlabProjectHook != null){
                                        gitlabAPI.deleteProjectHook(gitlabProjectHook);
                                        gitlabAPI.addProjectHook(gitlabProject,webhook);
                                    }
                                }
                            }else {
                                gitlabAPI.addProjectHook(gitlabProject,webhook);
                            }
                        }
                    }
                }else {
                    sequenceMapper.deleteByPrimaryKey(sequenceName);
                    dockerLogsMapper.deleteBy(container_name,dockerServer.getServer_ip());
                    containerDeployMapper.deleteBy(container_name,dockerServer.getServer_ip());
                    killBuild(giturl);
                    return new ResponseResult(false,500,"请设置gitlab验证",null);
                }
                //插入sequence自增序列
                sequenceMapper.insertSelective(new Sequence(sequenceName,1,1));
                //插入部署日志
                logs_name = container_name + "_" + dockerServer.getServer_ip() + "_" + 1;
                String logs_path = this.easyCiConfig.getLogpath() + "/" + container_name + "/" + logs_name;
                dockerLogsMapper.insertSelective(new DockerLogs(container_name,logs_name,1,logs_path,dockerServer.getServer_ip()));
            }else {
                //不是首次部署
                Integer lognum = sequenceMapper.selectNextVal(sequenceName);
                //插入部署日志
                logs_name = container_name + "_" + dockerServer.getServer_ip() + "_" + lognum;
                String logs_path = this.easyCiConfig.getLogpath() + "/" + container_name + "/" + logs_name;
                dockerLogsMapper.insertSelective(new DockerLogs(container_name,logs_name,lognum,logs_path,dockerServer.getServer_ip()));
            }
            //查看同一部署ip下容器名称不可相同
            List<ContainerDeploy> deploys = containerDeployMapper.selectByNameAndIp(container_name,dockerServer.getServer_ip());
            if (deploys.size() == 0){
                //插入部署数据
                ContainerDeploy containerDeploy = new ContainerDeploy();
                containerDeploy.setContainer_name(container_name);
                containerDeploy.setDocker_hub(dockerHub);
                containerDeploy.setGiturl(giturl);
                containerDeploy.setLanguage(language);
                containerDeploy.setMails(mails);
                containerDeploy.setPorts(ports);
                containerDeploy.setDeploy_ip(dockerServer.getServer_ip());
                containerDeployMapper.insertSelective(containerDeploy);
            }
            String[] cmd = new String[]{"sh","-c","bash " + this.easyCiConfig.getShpath() + "/easy_ci.sh -g " + giturl + "  -p " + ports + " -l " + language + " -m " + mails + " -d " + dockerHub
                    + " -e " + this.easyCiConfig.getShpath() +  " -s " + this.easyCiConfig.getLogpath() + " -w " + this.easyCiConfig.getWorkpath() + " -n " + logs_name
                    + " -i " + dockerServer.getServer_ip() + " -u " + dockerServer.getServer_username() + " -r " + dockerServer.getServer_password()};
            System.out.println(Arrays.toString(cmd));
            ps = Runtime.getRuntime().exec(cmd);
//            int exitValue = ps.waitFor();
            //当返回值为0时表示执行成功
//            if (0 != exitValue){
//                killBuild(giturl);
//                return new ResponseResult(false,500,"easyci脚本执行失败","错误码：" + exitValue);
//            }
            InputStream is = new StreamGobbler(ps.getInputStream());
            BufferedReader brs = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = brs.readLine()) != null) {
                log.info(line);
                if (line.contains("错误码")){
                    String[] errorCode = line.split(":");
                    if (errorCode.length > 0 && !"0".equals(errorCode[1])){
                        killBuild(giturl);
                        dockerLogsService.rmBuildLog(container_name,dockerServer.getServer_ip());
                        sequenceMapper.deleteByPrimaryKey(sequenceName);
                        dockerLogsMapper.deleteBy(container_name,dockerServer.getServer_ip());
                        //containerDeployMapper.deleteBy(container_name,dockerServer.getServer_ip());
                        return new ResponseResult(false,500,"拉取代码失败",null);
                    }
                }
                switch (line){
                    case "容器远程回滚成功":
                    case "容器远程部署成功":
                        return new ResponseResult(true,200,line,null);
                    case "容器远程部署失败":
                    case "远程服务器连接失败":
                    case "镜像拉取失败":
                    case "本地端口已被占用":
                    case "远程端口已被占用":
                    case "系统错误":
                        killBuild(giturl);
                        dockerLogsService.rmBuildLog(container_name,dockerServer.getServer_ip());
                        sequenceMapper.deleteByPrimaryKey(sequenceName);
                        dockerLogsMapper.deleteBy(container_name,dockerServer.getServer_ip());
                        //containerDeployMapper.deleteBy(container_name,dockerServer.getServer_ip());
                        return new ResponseResult(false,500,line,null);
                }
            }
            brs.close();
            is.close();
            ps.destroy();
            killBuild(giturl);
            return new ResponseResult(false,500,"easyci脚本执行失败",null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(false,500,"系统错误",e);
        }
    }

    @Override
    public ResponseResult killBuild(String grepstring) {
        //如果是git url，转换为http url
        GitlabToken gitlabToken = gitlabTokenMapper.select();
//        if (grepstring.length() > 3 && "git".equals(grepstring.substring(0,3))) {
//            if (gitlabToken != null){
//                GitlabAPI gitlabAPI = GitlabAPI.connect(gitlabToken.getGiturl(),gitlabToken.getAccess_token(), TokenType.ACCESS_TOKEN);
//                List<GitlabProject> gitlabProjects = null;
//                try {
//                    gitlabProjects = gitlabAPI.getProjects();
//                    for (GitlabProject gitlabProject:gitlabProjects){
//                        if (gitlabProject.getSshUrl().equals(grepstring)){
//                            grepstring = gitlabProject.getHttpUrl();
//                            String[] git = grepstring.split("//");
//                            grepstring = git[0] + "//" + gitlabToken.getUsername() + ":" + gitlabToken.getPassword() + "@" + git[1];
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }else {
//                return new ResponseResult(false,500,"请设置gitlab验证",null);
//            }
//        }else if (grepstring.length() > 3 && "http".equals(grepstring.substring(0,3))) {
            String[] git = grepstring.split("//");
            grepstring = git[0] + "//" + gitlabToken.getUsername() + ":" + gitlabToken.getPassword() + "@" + git[1];
//        }else {
//            return new ResponseResult(false,500,"giturl格式不正确",null);
//        }
        String[] cmd = new String[]{"sh","-c","ps -ef  | grep " + grepstring + " | awk '{print $2}' | head -n 2 | xargs kill -9"};
        System.out.println(Arrays.toString(cmd));
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            if (0 != exitValue){
                return new ResponseResult(false,500,"结束进程命令失败","错误码:" + exitValue);
            }
            process.destroy();
            return new ResponseResult(true,200,"部署指令关闭成功",null);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseResult(false,500,"系统错误",e);
        }
    }

    @Override
    public ResponseResult dockerExec(String container_name, String server_ip, String cmd) {
        String desc = "";
        switch (cmd){
            case "start":
                desc = "启动";
                break;
            case "stop":
                desc = "停止";
                break;
            case "restart":
                desc = "重启";
                break;
            case "rm":
                desc = "销毁";
                break;
        }
        if ("本机".equals(server_ip)){
            if ("rm".equals(cmd) && "qdockerhub".equals(container_name)){
                return new ResponseResult(false,500,"docker私服不允许删除",null);
            }
            Process process = null;
            try {
                String[] execcmd = new String[]{"sh","-c","docker " + cmd + " " + container_name};
                process = Runtime.getRuntime().exec(execcmd);
                int exitValue = process.waitFor();
                if (0 != exitValue){
                    return new ResponseResult(false,500,"执行命令失败",null);
                }
                InputStream is = new StreamGobbler(process.getInputStream());
                BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                String line = "";
                while ((line = brs.readLine()) != null) {
                    if (container_name.equals(line)){
                        return new ResponseResult(true,200,"容器" + container_name + desc +"成功",null);
                    }
                }
                brs.close();
                is.close();
                process.destroy();
                return new ResponseResult(false,500,"容器" + container_name + desc +"失败",null);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseResult(false,500,"系统错误",e);
            }
        }{
            Connection con = null;
            DockerServer dockerServer = new DockerServer();
            dockerServer.setServer_ip(server_ip);
            DockerServer dockerServer1 = dockerServerService.selectByDockerServer(dockerServer);
            con = ConnectUtil.getConnect(dockerServer1.getServer_ip(),dockerServer1.getServer_username(),dockerServer1.getServer_password(), Integer.parseInt(dockerServer1.getServer_port()));
            if (con != null) {
                Session ss = null;
                try {
                    String line = "";
                    ss = con.openSession();
                    String execcmd = "docker " + cmd + " " + container_name;
                    ss.execCommand(execcmd);
                    InputStream is = new StreamGobbler(ss.getStdout());
                    BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                    while ((line = brs.readLine()) != null) {
                        if (container_name.equals(line)){
                            if ("rm".equals(cmd)){
                                String sequenceName = container_name + ":" + server_ip;
                                dockerLogsService.rmBuildLog(container_name,server_ip);
                                sequenceMapper.deleteByPrimaryKey(sequenceName);
                                dockerLogsMapper.deleteBy(container_name,server_ip);
                                //containerDeployMapper.deleteBy(container_name,server_ip);
                            }
                            return new ResponseResult(true,200,"容器" + container_name + desc +"成功",null);
                        }
                    }
                    brs.close();
                    is.close();
                    ss.close();
                    con.close();
                    return new ResponseResult(false,500,"容器" + container_name + desc +"失败",null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseResult(false,500,"系统错误",e);
                }
            }else {
                return new ResponseResult(false,400,"连接服务器失败",null);
            }
        }
    }
}
