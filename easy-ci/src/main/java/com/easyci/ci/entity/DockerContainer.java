package com.easyci.ci.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class DockerContainer implements Serializable {

    private Integer id;

    private String container_id;

    private String image_name;

    private String command;

    private String created;

    private String status;

    private String ports;

    private String container_name;

    private String server_ip;

    private static final long serialVersionUID = 1L;

    public DockerContainer(String container_id, String image_name, String command, String created, String status, String ports, String container_name, String server_ip) {
        this.container_id = container_id;
        this.image_name = image_name;
        this.command = command;
        this.created = created;
        this.status = status;
        this.ports = ports;
        this.container_name = container_name;
        this.server_ip = server_ip;
    }
}