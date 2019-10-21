package com.easyci.ci.service;

import com.easyci.ci.entity.DockerContainer;
import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;

import java.util.List;

public interface DockerContainerService {

   /* 
    * @Author jiangxd 
    * @Description 初始化获取所有已添加服务器的容器列表
    * @Date 18:59 2019-10-05 
    * @Param  
    * @return  
   **/
    ResponseResult getAllDockerPs();

    /*
     * @Author jxd
     * @Description 获取主机运行的docker容器
     * @Date 10:53 2019/10/16
     * @Param []
     * @return java.util.List<com.easyci.ci.entity.DockerContainer>
     **/
    List<DockerContainer> localDockerPs();

    /* 
     * @Author jiangxd 
     * @Description 根据服务器信息查询该服务器的dockerps 
     * @Date 19:08 2019-10-05 
     * @Param  
     * @return  
    **/
    List<DockerContainer> getDockerPs(String hostName, String username, String password, Integer port);

    /* 
     * @Author jiangxd 
     * @Description 根据deploy_way选择本地部署或者远程部署,根据buildOrLogs选择功能类型
     * @Date 20:11 2019-10-05 
     * @Param  buildOrLogs: build,logs
     * @return  
    **/
    ResponseResult easyci(String giturl, String ports, String language, String mails, String deploy_way, String buildOrLogs,Integer start);

    /* 
     * @Author jiangxd 
     * @Description 接受参数执行脚本部署 
     * @Date 11:27 2019-10-08 
     * @Param  
     * @return  
    **/
    ResponseResult build(String giturl, String ports, String language, String mails, DockerServer dockerServer);

    /*
     * @Author jxd
     * @Description 杀掉build进程
     * @Date 11:37 2019/10/16
     * @Param
     * @return
     **/
     ResponseResult killBuild(String grepstring);

    /*
     * @Author jxd
     * @Description 操作容器，启动、停止、重启
     * @Date 14:21 2019/10/11
     * @Param
     * @return
     **/
    ResponseResult dockerExec(String container_name,String server_ip,String cmd);

}
