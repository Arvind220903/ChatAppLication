package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.TagsEntity;

@Repository
public interface TagsRepo extends JpaRepository<TagsEntity, Integer>{

	TagsEntity findByTags(String tag);

}
