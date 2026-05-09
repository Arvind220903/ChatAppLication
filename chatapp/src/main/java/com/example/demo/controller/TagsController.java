package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;
import com.example.demo.service.TagsService;

@RestController
@RequestMapping("/tags")
public class TagsController {
		@Autowired
		TagsService tagsService;
		@GetMapping("/trendingTags")
		public List<TagsEntity> getTrending(){
			return tagsService.getTrending();
		}
		@GetMapping("/findtags/{tag}")
		public List<TagsEntity> findTag(@PathVariable("tag") String tag){
			return tagsService.tags(tag);
		}
		@GetMapping("/getpostsbytag/{tag}")
		public List<PostEntity> getPostsByTag(@PathVariable("tag") String tag){
			return tagsService.getPostsByTag(tag);
		}
}
