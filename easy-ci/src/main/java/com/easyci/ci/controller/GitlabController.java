package com.easyci.ci.controller;

import com.easyci.ci.api.GitlabAPI;
import com.easyci.ci.api.TokenType;
import com.easyci.ci.api.models.GitlabProject;
import com.easyci.ci.dao.ContainerDeployMapper;
import com.easyci.ci.dao.GitlabTokenMapper;
import com.easyci.ci.entity.ContainerDeploy;
import com.easyci.ci.entity.GitlabToken;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.DockerContainerService;
import com.easyci.ci.service.GitlabTokenService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("gitlab")
public class GitlabController {

    @Autowired
    private GitlabTokenService gitlabTokenService;
    @Autowired
    private GitlabTokenMapper gitlabTokenMapper;
    @Autowired
    private ContainerDeployMapper containerDeployMapper;
    @Autowired
    private DockerContainerService dockerContainerService;

    /**
     * @Author jxd
     * @Description 获取所有gitlab仓库
     * @Date 13:45 2019/9/30
     * @Param []
     * @return com.easyci.ci.entity.ResponseResult
     **/
    @PostMapping("projects")
    public ResponseResult getGitlabProJects() {
        GitlabToken gitlabToken = gitlabTokenMapper.select();
        if (gitlabToken != null){
            GitlabAPI connect = null;
            try {
                connect = GitlabAPI.connect(gitlabToken.getGiturl(), gitlabToken.getAccess_token(), TokenType.ACCESS_TOKEN);
                List<GitlabProject> projects = connect.getProjects();
                return new ResponseResult(true,200,"获取成功",projects);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseResult(false,500,"获取失败",e);
            }
        }else {
            return new ResponseResult(false,500,"请设置系统信息",null);
        }
    }

    public static void main(String[] args) {
        GitlabAPI connect = null;
        try {
            connect = GitlabAPI.connect("http://192.168.8.10", "92b713c90f59cacbbfe7ad4038e8bd6f1a0da47cbef1bf00c79561927a3f6c7c", TokenType.ACCESS_TOKEN);
            List<GitlabProject> projects = connect.getProjects();
            System.out.println(projects.get(0).getLastActivityAt());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Author jxd
     * @Description gitlab自动触发构建
     * @Date 12:31 2019/9/30
     * @Param [request]
     * @return void
     **/
    @PostMapping("hook")
    public void gitlabhook(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                JSONObject jo = new JSONObject((str));
//                String git_url = jo.getJSONObject("repository").getString("url");
                String git_http_url = jo.getJSONObject("repository").getString("git_http_url");
//                System.out.println(git_url);
                String project_name = jo.getJSONObject("repository").getString("name");
//                String project_id = jo.getJSONObject("project").getString("id");
                List<ContainerDeploy> deploys = containerDeployMapper.selectByConName(project_name);
                if (deploys.size() > 0){
                    for (ContainerDeploy containerDeploy:deploys){
                        System.out.println(containerDeploy);
                        if (StringUtils.isEmpty(containerDeploy.getMails()))
                            containerDeploy.setMails("admin@mingbyte.com");
                        dockerContainerService.easyci(git_http_url,containerDeploy.getPorts(),containerDeploy.getLanguage(),containerDeploy.getMails(),containerDeploy.getDeploy_ip(),"build",null);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @Author jxd
     * @Description 登录验证gitlab，获得密钥
     * @Date 13:45 2019/10/12
     * @Param [username, password]
     * @return com.easyci.ci.entity.ResponseResult
     **/
    @PostMapping("login")
    public ResponseResult gitlabLogin(String githost, String username,String password) {
        GitlabToken gitlabToken = gitlabTokenService.selectByUsername(username);
        ResponseResult r = gitlabTokenService.getAccessToken("http://" + githost,username,password);
        if (r.getStatus()){
            GitlabToken gitlabToken1 = new GitlabToken();
            gitlabToken1.setGiturl("http://" + githost);
            gitlabToken1.setUsername(username);
            gitlabToken1.setPassword(password);
            gitlabToken1.setAccess_token(String.valueOf(r.getList()));
            if (gitlabToken == null){
                if (gitlabTokenMapper.insertSelective(gitlabToken1) > 0){
                    return new ResponseResult(true,200,"登陆成功",null);
                }else {
                    return new ResponseResult(false,500,"登陆失败",null);
                }
            }else {
                gitlabTokenMapper.updateByPrimaryKeySelective(gitlabToken1);
                return new ResponseResult(true,200,"登录成功",null);
            }
        }else {
            return new ResponseResult(false,500,r.getErrorDesc(),null);
        }
    }

    /**
     * @Author jxd
     * @Description 验证是否设置配置信息
     * @Date 11:35 2019/10/12
     * @Param
     * @return
     **/
    @PostMapping("isSet")
    public ResponseResult IsSetSystemSetting() {
        GitlabToken gitlabToken = gitlabTokenMapper.select();
        if (gitlabToken != null) {
            return new ResponseResult(true, 200, "配置信息已设置", null);
        } else {
            return new ResponseResult(false, 500, "请设置配置信息", null);
        }
    }
}
