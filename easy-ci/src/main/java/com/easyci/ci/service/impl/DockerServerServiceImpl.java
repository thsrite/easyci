package com.easyci.ci.service.impl;

import ch.ethz.ssh2.Connection;
import com.easyci.ci.dao.DockerServerMapper;
import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.DockerServerService;
import com.easyci.ci.util.ConnectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dockerServerService")
public class DockerServerServiceImpl implements DockerServerService {

    @Autowired
    private DockerServerMapper dockerServerMapper;
    @Autowired
    private DockerRemoteServiceImpl dockerRemoteService;

    @Override
    public ResponseResult selectSearchServer() {
        List<DockerServer> list = dockerServerMapper.selectSearchServer();
        if (list.size() > 0){
            return new ResponseResult(true,200,"查询成功",list);
        }else {
            return new ResponseResult(true,200,"暂无服务器，请新增！",null);
        }
    }

    @Override
    public ResponseResult selectDeployServer() {
        List<DockerServer> list = dockerServerMapper.selectDeployServer();
        if (list.size() > 0) {
            return new ResponseResult(true, 200, "查询成功", list);
        } else {
            return new ResponseResult(true, 200, "暂无服务器，请新增！", null);
        }
    }

    @Override
    public ResponseResult insertSelective(DockerServer dockerServer) {
        try {
            Connection connect = ConnectUtil.getConnect(dockerServer.getServer_ip(), dockerServer.getServer_username(), dockerServer.getServer_password(), Integer.parseInt(dockerServer.getServer_port()));
            if (connect != null && connect.isAuthenticationComplete()){
                DockerServer server = new DockerServer();
                server.setServer_ip(dockerServer.getServer_ip());
                DockerServer dockerServer1 = dockerServerMapper.selectByDockerServer(server);
                if (dockerServer1 == null){
                    if (dockerRemoteService.setAuthorizedKeys(dockerServer).getStatus()){
                        if (dockerServerMapper.insertSelective(dockerServer) > 0 ){
                            return new ResponseResult(true,200,"免密登录认证成功",null);
                        }else {
                            return new ResponseResult(true,300,"插入失败",null);
                        }
                    }else {
                        return new ResponseResult(true,200,"免密登录认证失败",null);
                    }
                }else {
                    return new ResponseResult(true,300,"该服务器已存在",null);
                }
            }else {
                return new ResponseResult(false,500,"服务器登录认证失败",null);
            }
        }catch (Exception e){
            return new ResponseResult(false,500,"服务器登录认证失败",null);
        }
    }

    @Override
    public DockerServer selectByDockerServer(DockerServer record) {
        return dockerServerMapper.selectByDockerServer(record);
    }

    @Override
    public List<DockerServer> selectServers() {
        return dockerServerMapper.selectServers();
    }

    @Override
    public DockerServer selectLocalServer() {
        return dockerServerMapper.selectLocalServer();
    }

//    @Override
//    public ResponseResult IsSetLocalPwd() {
//        DockerServer dockerServer = dockerServerMapper.selectLocalServer();
//        if (dockerServer == null){
//            InetAddress addr = null;
//            try {
//                addr = InetAddress.getLocalHost();
//                return new ResponseResult(false,500,"请验证主机信息",addr.getHostAddress());
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//                return new ResponseResult(false,500,"系统错误",null);
//            }
//        }else {
//            return new ResponseResult(true,200,"主机信息已设置",null);
//        }
//    }
}
