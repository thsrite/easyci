package com.easyci.ci.service;

import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;

public interface DockerRemoteService {

    /*
     * @Author jxd
     * @Description 读取本机公钥
     * @Date 15:38 2019/10/11
     * @Param
     * @return
     **/
    ResponseResult getPubRsa();

    /*
     * @Author jxd
     * @Description 设置远程authorized_keys
     * @Date 16:09 2019/10/11
     * @Param
     * @return
     **/
    ResponseResult setAuthorizedKeys(DockerServer dockerServer);
}
