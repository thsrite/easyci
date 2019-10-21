package com.easyci.ci.dao;

import com.easyci.ci.entity.DockerContainer;

public interface DockerContainerMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(DockerContainer record);

    int insertSelective(DockerContainer record);

    DockerContainer selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DockerContainer record);

    int updateByPrimaryKey(DockerContainer record);

}