package com.easyci.ci.controller;

import com.easyci.ci.dao.GitlabTokenMapper;
import com.easyci.ci.entity.DockerContainer;
import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.DockerContainerService;
import com.easyci.ci.service.DockerLogsService;
import com.easyci.ci.service.DockerServerService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("docker")
public class ContainerController {

    @Autowired
    private DockerServerService dockerServerService;
    @Autowired
    private DockerContainerService dockerContainerService;
    @Autowired
    private DockerLogsService dockerLogsService;
    @Autowired
    private GitlabTokenMapper gitlabTokenMapper;

    /**
     * @return java.lang.Object
     * @Author jxd
     * @Description 初始化所有的容器列表
     * @Date 11:39 2019/9/29
     * @Param [hostName, username, password, port]
     **/
    @PostMapping("allps")
    public ResponseResult getAllDockerPs() {
        return dockerContainerService.getAllDockerPs();
    }

    /**
     * @Author jxd
     * @Description 获取本机docker容器列表
     * @Date 10:57 2019/10/16
     * @Param
     * @return
     **/
    @PostMapping("localps")
    public ResponseResult getLocalDockerPs(){
        List<DockerContainer> dockerContainers = dockerContainerService.localDockerPs();
        if (dockerContainers.size() > 0){
            return new ResponseResult(true,200,"获取主机容器列表成功",dockerContainers);
        }else {
            return new ResponseResult(false,500,"初始化主机容器列表失败",null);
        }
    }

    /**
     * @Author jiangxd
     * @Description 根据选择的服务器ip查询dockerps列表
     * @Date 18:36 2019-10-05
     * @Param [serverip]
     * @return com.easyci.ci.entity.ResponseResult
    **/
    @PostMapping("oneps")
    public ResponseResult getOneDockerPs(@NonNull Integer serverid){
        DockerServer dockerServer = new DockerServer();
        dockerServer.setId(serverid);
        DockerServer dockerServer1 = dockerServerService.selectByDockerServer(dockerServer);
        if (dockerServer1 != null){
            List<DockerContainer> dockerContainers = dockerContainerService.getDockerPs(dockerServer1.getServer_ip(),dockerServer1.getServer_username(),dockerServer1.getServer_password(), Integer.valueOf(dockerServer1.getServer_port()));
            if (dockerContainers.size() > 0){
                return new ResponseResult(true,200,"查询" + dockerServer1.getServer_ip() + "服务器成功",dockerContainers);
            }else {
                return new ResponseResult(true,400,"查询失败,请检查服务器配置",null);
            }
        }else {
            return new ResponseResult(false,500,"系统错误",null);
        }
    }

    /**
     * @Author jxd
     * @Description 调用linux脚本，自动化部署服务
     * @Date 16:43 2019/9/30
     * @Param [giturl, ports, language, maillists]
     * @return com.easyci.ci.entity.ResponseResult
     **/
    @PostMapping("build")
    public ResponseResult buildProject(String giturl, String ports, String language, String mails, String deploy_way){
        return dockerContainerService.easyci(giturl, ports, language, mails, deploy_way,"build",null);
    }

    /**
     * @Author jxd
     * @Description kill build进程
     * @Date 11:43 2019/10/16
     * @Param
     * @return
     **/
    @PostMapping("killbuild")
    public ResponseResult killBuild(String giturl){
        return dockerContainerService.killBuild(giturl);
    }

    /**
     * @Author jiangxd
     * @Description 查看容器部署日志
     * @Date 13:52 2019-10-08
     * @Param
     * @return
    **/
    @PostMapping("deployLogs")
    public ResponseResult getDeployLogs(String giturl,String deploy_way,Integer start){
        return dockerContainerService.easyci(giturl,"","","",deploy_way,"logs",start);
    }

    /**
     * @Author jxd
     * @Description docker logs -f 实时查看docker容器日志
     * @Date 11:08 2019/10/10
     * @Param
     * @return
     **/
    @PostMapping("dockerLogs")
    public ResponseResult getDockerLogs(String container_name, String server_ip,String random){
        return dockerLogsService.dockerLogs(container_name,server_ip,random);
    }

   /**
    * @Author jxd
    * @Description 杀掉docker log -f 进程
    * @Date 16:48 2019/10/14
    * @Param [container_name, server_ip]
    * @return com.easyci.ci.entity.ResponseResult
    **/
    @PostMapping("killLogs")
    public ResponseResult killDockerLogs(String container_name, String server_ip){
        return dockerLogsService.killDockerLogs(container_name, server_ip);
    }

    /**
     * @Author jxd
     * @Description docker start|stop|restart 容器
     * @Date 14:32 2019/10/11
     * @Param
     * @return
     **/
    @PostMapping("dockerExec")
    public ResponseResult dockerExec(String container_name, String server_ip, String cmd){
        return dockerContainerService.dockerExec(container_name, server_ip, cmd);
    }
}