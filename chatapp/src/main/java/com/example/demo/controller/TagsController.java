package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.TagsEntity;
import com.example.demo.service.TagsService;

@RestController
public class TagsController {
		@Autowired
		TagsService tagsService;
		@GetMapping("/trendingTags")
		public List<TagsEntity> getTrending(){
			return tagsService.getTrending();
		}
}
