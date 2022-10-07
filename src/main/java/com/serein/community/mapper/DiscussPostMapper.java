package com.serein.community.mapper;

import com.serein.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DiscussPostMapper {
//    List<DiscussPost> selectDiscussPosts(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    List<DiscussPost> selectDiscussPosts(@Param("userId") Long userId);

    int selectDiscussPostRows(Long userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(Long id);

    int updateCommentCount(@Param("id") Long id,@Param("commentCount") int commentCount);

    int updateType(@Param("id")Long id,@Param("type") int type);

    int updateStatus(@Param("id")Long id,@Param("status") int status);
}
