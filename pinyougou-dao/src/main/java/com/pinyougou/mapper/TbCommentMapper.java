package com.pinyougou.mapper;

import java.util.List;

import com.pinyougou.pojo.TbComment;
import com.pinyougou.pojo.TbCommentExample;
import org.apache.ibatis.annotations.Param;


public interface TbCommentMapper {
    long countByExample(TbCommentExample example);

    int deleteByExample(TbCommentExample example);

    int deleteByPrimaryKey(String id);

    int insert(TbComment record);

    int insertSelective(TbComment record);

    List<TbComment> selectByExampleWithBLOBs(TbCommentExample example);

    List<TbComment> selectByExample(TbCommentExample example);

    TbComment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TbComment record, @Param("example") TbCommentExample example);

    int updateByExampleWithBLOBs(@Param("record") TbComment record, @Param("example") TbCommentExample example);

    int updateByExample(@Param("record") TbComment record, @Param("example") TbCommentExample example);

    int updateByPrimaryKeySelective(TbComment record);

    int updateByPrimaryKeyWithBLOBs(TbComment record);

    int updateByPrimaryKey(TbComment record);
}