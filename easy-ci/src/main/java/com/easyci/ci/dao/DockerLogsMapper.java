package com.easyci.ci.dao;

import com.easyci.ci.entity.DockerLogs;

import java.util.List;

public interface DockerLogsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DockerLogs record);

    int insertSelective(DockerLogs record);

    DockerLogs selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DockerLogs record);

    int updateByPrimaryKey(DockerLogs record);

    /*
     * @Author jxd
     * @Description 根据容器名称和部署ip删除
     * @Date 13:49 2019/10/11
     * @Param [docker_name, deploy_ip]
     * @return int
     **/
    int deleteBy(String docker_name,String deploy_ip);

    /*
     * @Author jxd
     * @Description 根据容器名称和部署ip查询日志
     * @Date 17:27 2019/10/19
     * @Param [docker_name, deploy_ip]
     * @return java.util.List<com.easyci.ci.entity.DockerLogs>
     **/
    List<DockerLogs> selectBy(String docker_name,String deploy_ip);
}