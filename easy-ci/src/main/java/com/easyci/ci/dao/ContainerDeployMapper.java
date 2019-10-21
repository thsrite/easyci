package com.easyci.ci.dao;

import com.easyci.ci.entity.ContainerDeploy;

import java.util.List;

public interface ContainerDeployMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ContainerDeploy record);

    int insertSelective(ContainerDeploy record);

    ContainerDeploy selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ContainerDeploy record);

    int updateByPrimaryKey(ContainerDeploy record);

    /*
     * @Author jxd
     * @Description 根据容器名称和部署ip删除
     * @Date 13:49 2019/10/11
     * @Param [container_name, deploy_ip]
     * @return int
     **/
    int deleteBy(String container_name,String deploy_ip);

    /*
     * @Author jxd
     * @Description 根据容器名称查看容器部署记录，用于gitlab hook调用
     * @Date 17:31 2019/10/14
     * @Param [container_name]
     * @return java.util.List<com.easyci.ci.entity.ContainerDeploy>
     **/
    List<ContainerDeploy> selectByConName(String container_name);

    /*
     * @Author jxd
     * @Description 根据容器名和部署ip唯一确定一条数据
     * @Date 17:37 2019/10/14
     * @Param [container_name, deploy_ip]
     * @return java.util.List<com.easyci.ci.entity.ContainerDeploy>
     **/
    List<ContainerDeploy> selectByNameAndIp(String container_name,String deploy_ip);
}