package com.easyci.ci.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContainerDeploy implements Serializable {

    private Integer id;

    private String container_name;

    private String giturl;

    private String ports;

    private String language;

    private String mails;

    private String docker_hub;

    private String deploy_ip;

    private static final long serialVersionUID = 1L;

}