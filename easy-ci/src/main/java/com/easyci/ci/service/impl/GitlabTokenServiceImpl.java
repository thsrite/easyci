package com.easyci.ci.service.impl;

import ch.ethz.ssh2.StreamGobbler;
import com.alibaba.fastjson.JSONObject;
import com.easyci.ci.dao.GitlabTokenMapper;
import com.easyci.ci.config.EasyCiConfig;
import com.easyci.ci.entity.GitlabToken;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.GitlabTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service("gitlabService")
public class GitlabTokenServiceImpl implements GitlabTokenService {

    @Autowired
    private GitlabTokenMapper gitlabTokenMapper;
    @Autowired
    private EasyCiConfig easyCiConfig;

    @Override
    public ResponseResult getAccessToken(String giturl, String username, String password) {
        Process ps = null;
        try {
            String cmd = "bash " + this.easyCiConfig.getShpath() + "/gitlab_token.sh " + username + " " + password + " " + giturl;
            ps = Runtime.getRuntime().exec(cmd);
            int exitValue = ps.waitFor();
            //当返回值为0时表示执行成功
            if (0 != exitValue){
                return new ResponseResult(false,500,"命令执行失败","错误码：" + exitValue);
            }
            InputStream is = new StreamGobbler(ps.getInputStream());
            BufferedReader brs = new BufferedReader(new InputStreamReader(is));
            String line = "";
            String readline = "";
            while ((line = brs.readLine()) != null) {
                readline = readline + line;
            }
            JSONObject jsonObject = JSONObject.parseObject(readline);
            brs.close();
            is.close();
            ps.destroy();
            System.out.println(jsonObject);
            if (jsonObject.getString("access_token") != null){
                return new ResponseResult(true,200,"获取gitlab密钥成功",jsonObject.getString("access_token"));
            }else {
                return new ResponseResult(false,500,"用户验证失败",null);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public GitlabToken selectByUsername(String username) {
        return gitlabTokenMapper.selectByUsername(username);
    }
}
