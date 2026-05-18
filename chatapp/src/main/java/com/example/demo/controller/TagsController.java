package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	public List<TagsEntity> getTrending(@RequestParam int pageNumber,@RequestParam int pageSize) {
		return tagsService.getTrending(pageNumber,pageSize);
	}

	@GetMapping("/findtags")
	public List<TagsEntity> findTag(@RequestParam("tag") String tag) {
		return tagsService.tags(tag);
	}

	@GetMapping("/getpostsbytag")
	public List<PostEntity> getPostsByTag(@RequestParam("tag") String tag) {
		return tagsService.getPostsByTag(tag);
	}
}
