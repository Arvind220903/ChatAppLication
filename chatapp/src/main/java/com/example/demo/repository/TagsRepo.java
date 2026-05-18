package com.example.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.TagsEntity;

@Repository
public interface TagsRepo extends JpaRepository<TagsEntity, Integer> {

	TagsEntity findByTags(String tag);
	TagsEntity findFirstByTags(String tag);

	@Query("select t from TagsEntity as t where t.tags like concat ('%', :keyword ,'%')")
	public List<TagsEntity> getAllTags(@Param("keyword") String keyword);
	@Query("select t from TagsEntity t where t.tags in :tags")
	public List<TagsEntity> findBatchByTags(@Param("tags") List<String> tags);

}
