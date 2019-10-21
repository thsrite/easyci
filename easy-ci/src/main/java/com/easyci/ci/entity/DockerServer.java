package com.easyci.ci.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DockerServer implements Serializable {

    private Integer id;

    private String server_ip;

    private String server_port;

    private String server_username;

    private String server_password;

    private Integer is_local;

    private static final long serialVersionUID = 1L;
}