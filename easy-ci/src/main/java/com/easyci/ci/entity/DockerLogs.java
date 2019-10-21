package com.easyci.ci.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DockerLogs implements Serializable {
    private Integer id;

    private String docker_name;

    private String docker_logs;

    private Integer logs_num;

    private String logs_path;

    private String deploy_ip;

    private static final long serialVersionUID = 1L;

    public DockerLogs(){

    }

    public DockerLogs(String docker_name, String docker_logs, Integer logs_num, String logs_path, String deploy_ip) {
        this.docker_name = docker_name;
        this.docker_logs = docker_logs;
        this.logs_num = logs_num;
        this.logs_path = logs_path;
        this.deploy_ip = deploy_ip;
    }
}