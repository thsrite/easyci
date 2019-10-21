package com.easyci.ci.service;

import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;

import java.util.List;

public interface DockerServerService {

    /*
     * @Author jiangxd
     * @Description 前端查看已添加的服务器，选择查看该服务器的容器列表
     * @Date 18:47 2019-10-05
     * @Param
     * @return
     **/
    ResponseResult selectSearchServer();

    /*
     * @Author jiangxd
     * @Description 前端查看已添加的服务器，选择部署
     * @Date 18:47 2019-10-05
     * @Param
     * @return
     **/
    ResponseResult selectDeployServer();
    /*
     * @Author jiangxd
     * @Description 新增服务器，存库
     * @Date 18:47 2019-10-05
     * @Param
     * @return
     **/
    ResponseResult insertSelective(DockerServer dockerServer);

    /*
     * @Author jiangxd
     * @Description 根据服务器的各种参数查找服务器
     * @Date 18:48 2019-10-05
     * @Param
     * @return
     **/
    DockerServer selectByDockerServer(DockerServer record);

    /*
     * @Author jiangxd
     * @Description 获取所有服务器，用于dockerps
     * @Date 18:48 2019-10-05
     * @Param
     * @return
     **/
    List<DockerServer> selectServers();

    /*
     * @Author jxd
     * @Description 获取本机ip用于获取公钥
     * @Date 15:43 2019/10/11
     * @Param []
     * @return com.easyci.ci.entity.DockerServer
     **/
    DockerServer selectLocalServer();

//    /*
//     * @Author jxd
//     * @Description 验证是否设置本机ip密码
//     * @Date 11:28 2019/10/12
//     * @Param
//     * @return
//     **/
//    ResponseResult IsSetLocalPwd();
}
