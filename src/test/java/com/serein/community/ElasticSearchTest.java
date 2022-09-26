package com.serein.community;

import com.serein.community.entity.DiscussPost;
import com.serein.community.mapper.DiscussPostMapper;
import com.serein.community.mapper.elasticsearch.DiscussPostRepository;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.List;

@SpringBootTest
public class ElasticSearchTest {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241L));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242L));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243L));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133L));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134L));
    }

    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(241L);
        discussPost.setContent("我是新人");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void testDelete(){
        discussPostRepository.deleteById(231);
//        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 多个字段查询
                .withQuery(QueryBuilders.multiMatchQuery("互联网", "title", "content"))
                // 排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页
                .withPageable(PageRequest.of(3, 10))
                // 高亮
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);

        int i = 1;
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            DiscussPost discussPost = searchHit.getContent();
            System.out.println(discussPost);
            System.out.println(i++);
        }
    }
}
