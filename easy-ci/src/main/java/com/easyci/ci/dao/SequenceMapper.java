package com.easyci.ci.dao;

import com.easyci.ci.entity.Sequence;

public interface SequenceMapper {
    int deleteByPrimaryKey(String name);

    int insert(Sequence record);

    int insertSelective(Sequence record);

    Sequence selectByPrimaryKey(String name);

    int updateByPrimaryKeySelective(Sequence record);

    int updateByPrimaryKey(Sequence record);

    //获取下一个值
    Integer selectNextVal(String name);
    //设置值
    int setNextVal(String name,int nextval);
    //查询当前值
    Integer selectCurrentVal(String name);
}