package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;


import com.example.demo.entity.TagsEntity;

@Service
public interface TagsService {
	public List<TagsEntity> tags();
	
	public void refresh();
	public List<TagsEntity> getTrending();
}
