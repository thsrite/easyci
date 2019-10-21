package com.easyci.ci.service;

import com.easyci.ci.entity.GitlabToken;
import com.easyci.ci.entity.ResponseResult;

public interface GitlabTokenService {

    /*
     * @Author jxd
     * @Description 根据gitlab url，username，password获取access_token
     * @Date 11:20 2019/10/12
     * @Param
     * @return
     **/
    ResponseResult getAccessToken(String giturl,String username,String password);

    /*
     * @Author jxd
     * @Description //TODO
     * @Date 13:35 2019/10/12
     * @Param
     * @return
     **/
    GitlabToken selectByUsername(String username);
}
