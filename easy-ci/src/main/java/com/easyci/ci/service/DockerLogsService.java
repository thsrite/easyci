package com.easyci.ci.service;

import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;

public interface DockerLogsService {

    /*
     * @Author jiangxd
     * @Description 查看docker容器日志
     * @Date 10:28 2019-10-08
     * @Param
     * @return
     **/
    ResponseResult catchServerLog(String giturl, Integer start, DockerServer dockerServer);

    /*
     * @Author jxd
     * @Description 实时查看docker logs 日志
     * @Date 11:03 2019/10/10
     * @Param [container_name, server_ip]
     * @return com.easyci.ci.entity.ResponseResult
     **/
    ResponseResult dockerLogs(String container_name,String server_ip,String random);

    /*
     * @Author jxd
     * @Description 关闭实时日志
     * @Date 16:45 2019/10/14
     * @Param
     * @return
     **/
    ResponseResult killDockerLogs(String container_name,String server_ip);

    /*
     * @Author jxd
     * @Description 销毁容器后删除日志
     * @Date 13:03 2019/10/16
     * @Param
     * @return
     **/
    ResponseResult rmBuildLog(String docker_name,String deploy_ip);
}
