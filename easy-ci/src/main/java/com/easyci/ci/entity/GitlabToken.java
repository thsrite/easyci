package com.easyci.ci.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class GitlabToken implements Serializable {
    private Integer id;

    private String username;

    private String password;

    private String giturl;

    private String access_token;

    private static final long serialVersionUID = 1L;

}