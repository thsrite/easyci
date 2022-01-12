package com.easyci.ci.controller;

import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.DockerServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("server")
public class ServerController {

    @Autowired
    private DockerServerService dockerServerService;


    /**
     * @Author jiangxd
     * @Description 用于查询服务器的容器列表
     * @Date 13:21 2019-10-01
     * @Param []
     * @return com.easyci.ci.entity.ResponseResult
    **/
    @GetMapping("searchServerList")
    public ResponseResult getSearchServerList(){
        return dockerServerService.selectSearchServer();
    }

    /**
     * @Author jiangxd
     * @Description 用于可进行部署的服务器列表
     * @Date 13:21 2019-10-01
     * @Param []
     * @return com.easyci.ci.entity.ResponseResult
     **/
    @GetMapping("deployServerList")
    public ResponseResult getDeployServerList(){
        return dockerServerService.selectDeployServer();
    }

    /**
     * @Author jiangxd
     * @Description 新增服务器
     * @Date 13:22 2019-10-01
     * @Param [dockerServer]
     * @return com.easyci.ci.entity.ResponseResult
    **/
    @PostMapping("add")
    public ResponseResult insertServer(DockerServer dockerServer){
        return dockerServerService.insertSelective(dockerServer);
    }


}
