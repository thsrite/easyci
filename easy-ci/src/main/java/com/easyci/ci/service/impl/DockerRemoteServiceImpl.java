package com.easyci.ci.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.easyci.ci.config.EasyCiConfig;
import com.easyci.ci.entity.DockerServer;
import com.easyci.ci.entity.ResponseResult;
import com.easyci.ci.service.DockerRemoteService;
import com.easyci.ci.util.ConnectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


@Service("dockerRemoteService")
public class DockerRemoteServiceImpl implements DockerRemoteService {

    //主机公钥路径
    private static final String rsa_path = "/root/.ssh/id_rsa.pub";
    //远程机认证密钥路径
    private static final String authorized_keys = "/root/.ssh/authorized_keys";

    @Autowired
    private EasyCiConfig easyCiConfig;

    @Override
    public ResponseResult getPubRsa() {

        Process ps = null;
        try {
            String fileExist = "bash " + easyCiConfig.getShpath() + "/isExitFile.sh " + rsa_path;
            ps = Runtime.getRuntime().exec(fileExist);
            //当返回值为0时表示执行成功
            if (0 != ps.waitFor()) {
                return new ResponseResult(false, 500, "命令执行失败", "错误码：" + ps.waitFor());
            }
            InputStream is = new StreamGobbler(ps.getInputStream());
            BufferedReader brs = new BufferedReader(new InputStreamReader(is));
            String line = "";
            String error = "";
            while ((line = brs.readLine()) != null) {
                error = error + line;
            }
            is.close();
            brs.close();
            ps.destroy();
            if ("文件不存在".equals(error)){
                String[] cmd = new String[]{"sh","-c","ssh-keygen -t rsa -N '' -f /root/.ssh/id_rsa -q"};
                Process keygenPs = Runtime.getRuntime().exec(cmd);
                //当返回值为0时表示执行成功
                int exitValue = keygenPs.waitFor();
                if (0 != exitValue) {
                    return new ResponseResult(false, 500, "命令执行失败", "错误码：" + exitValue);
                }
                keygenPs.destroy();
            }
            //文件已存在
            String[] cmd1 = new String[]{"sh","-c","cat " + rsa_path};
            Process pubRsaPs = Runtime.getRuntime().exec(cmd1);
            //当返回值为0时表示执行成功
            int exitValue = pubRsaPs.waitFor();
            if (0 != exitValue) {
                return new ResponseResult(false, 500, "命令执行失败", "错误码：" + exitValue);
            }
            InputStream is1 = new StreamGobbler(pubRsaPs.getInputStream());
            BufferedReader brs1 = new BufferedReader(new InputStreamReader(is1));
            String line1 = "";
            String pubRsa = "";
            while ((line1 = brs1.readLine()) != null) {
                pubRsa = pubRsa + line1;
            }
            brs1.close();
            is1.close();
            pubRsaPs.destroy();
            return new ResponseResult(true, 200, "获取本机公钥成功", pubRsa);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseResult(false,500,"系统错误",e);
        }
    }

    @Override
    public ResponseResult setAuthorizedKeys(DockerServer dockerServer) {
        Connection connection = null;
        connection = ConnectUtil.getConnect(dockerServer.getServer_ip(),dockerServer.getServer_username(),dockerServer.getServer_password(), Integer.parseInt(dockerServer.getServer_port()));
        if (connection != null){
            Session ss = null;
            try {
                ss = connection.openSession();
                String cmd = "mkdir -p /root/.ssh";
                ss.execCommand(cmd);
                ss.close();
                if (getPubRsa().getStatus()){
                    Session session = connection.openSession();
                    String cmd1 = "echo '" + getPubRsa().getList() + "' >> " + authorized_keys;
                    session.execCommand(cmd1);
                    session.close();
                    Session session1 = connection.openSession();
                    session1.execCommand("echo $?");
                    InputStream is = new StreamGobbler(session1.getStdout());
                    BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                    String line = "";
                    String result = "";
                    while ((line = brs.readLine()) != null) {
                        result = result + line;
                    }
                    System.out.println(result);
                    brs.close();
                    is.close();
                    session1.close();
                    connection.close();
                    if ("0".equals(result)){
                        return new ResponseResult(true,200,"免密登录设置成功",null);
                    }else {
                        return new ResponseResult(false,500,"免密登录设置失败",null);
                    }
                }else {
                    return new ResponseResult(false,500,"获取主机公钥失败",null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseResult(false,500,"系统错误",e);
            }
        }else {
            return new ResponseResult(false,500,"连接服务器失败",null);
        }
    }
}
