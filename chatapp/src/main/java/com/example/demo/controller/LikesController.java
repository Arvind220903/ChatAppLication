package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.LikesService;

@CrossOrigin(origins = "http://localhost:4201")
@RestController
@RequestMapping("/likes")
public class LikesController {
	@Autowired
	private LikesService likeService;

	@PostMapping("/like")
	public String likePost(@RequestParam int userId, @RequestParam int postId) {
		return likeService.likePost(userId, postId);
	}

	@GetMapping("/getLikes/{postid}")
	public int getLikes(@PathVariable("postid") int postid) {
		return likeService.likeCount(postid);
	}
}
