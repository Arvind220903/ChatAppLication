package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PostEntity;
import com.example.demo.jwt.JwtService;
import com.example.demo.service.LikesService;

@RestController
@RequestMapping("/likes")
public class LikesController {
	@Autowired
	private LikesService likeService;
	@Autowired
	JwtService jwt;

	@PostMapping("/like/{postId}")
	public String likePost(@RequestHeader("Authorization") String token, @PathVariable int postId) {
		System.out.println("DEBUG LIKES: POST /likes/like/" + postId + " hit");
		String username = jwt.extractUsername(token.substring(7));
		System.out.println("DEBUG LIKES: user=" + username + ", postId=" + postId);
		String result = likeService.likePost(username, postId);
		System.out.println("DEBUG LIKES: result=" + result);
		return result;
	}

	@GetMapping("/getLikes/{postid}")
	public int getLikes(@PathVariable("postid") int postid) {
		return likeService.likeCount(postid);
	}
	@GetMapping("/getlikeposts")
	public List<PostEntity> getPostByLikes(@RequestHeader("Authorization") String token){
		String username=jwt.extractUsername(token.substring(7));
		return likeService.getByLikes(username);
	}
}
