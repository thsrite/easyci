package com.easyci.ci.dao;

import com.easyci.ci.entity.DockerServer;

import java.util.List;

public interface DockerServerMapper {

    List<DockerServer> selectSearchServer();

    List<DockerServer> selectDeployServer();

    int insertSelective(DockerServer record);

    DockerServer selectByDockerServer(DockerServer record);

    List<DockerServer> selectServers();

    int deleteByPrimaryKey(Integer id);

    int insert(DockerServer record);

    int updateByPrimaryKeySelective(DockerServer record);

    int updateByPrimaryKey(DockerServer record);

    DockerServer selectLocalServer();
}