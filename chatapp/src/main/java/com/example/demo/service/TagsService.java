package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;

@Service
public interface TagsService {
	public List<TagsEntity> tags(String t);
	
	public void refresh();
	public List<TagsEntity> getTrending();
	public List<PostEntity> getPostsByTag(String tag);
}
