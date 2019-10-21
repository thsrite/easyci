package com.easyci.ci.dao;

import com.easyci.ci.entity.GitlabToken;

public interface GitlabTokenMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(GitlabToken record);

    int insertSelective(GitlabToken record);

    GitlabToken selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GitlabToken record);

    int updateByPrimaryKey(GitlabToken record);

    GitlabToken selectByUsername(String username);

    GitlabToken select();
}