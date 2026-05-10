package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PostEntity;
import com.example.demo.service.LikesService;

@RestController
@RequestMapping("/likes")
public class LikesController {
	@Autowired
	private LikesService likeService;

	@PostMapping("/like/{postId}")
	public String likePost(Principal principal, @PathVariable int postId) {
		if (principal == null) return "User not authenticated";
		return likeService.likePost(principal.getName(), postId);
	}

	@GetMapping("/getLikes/{postid}")
	public int getLikes(@PathVariable("postid") int postid) {
		return likeService.likeCount(postid);
	}

	@GetMapping("/getlikeposts")
	public List<PostEntity> getPostByLikes(Principal principal){
		if (principal == null) return null;
		return likeService.getByLikes(principal.getName());
	}
}
