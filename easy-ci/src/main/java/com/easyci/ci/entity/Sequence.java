package com.easyci.ci.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Sequence implements Serializable {

    private String name;

    private Integer current_value;

    private Integer increment;

    private static final long serialVersionUID = 1L;
}