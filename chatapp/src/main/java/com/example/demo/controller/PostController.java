package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PostEntity;
import com.example.demo.jwt.JwtService;
import com.example.demo.service.PostService;
@CrossOrigin(origins = "http://localhost:4201")
@RestController
@RequestMapping("/posts")
public class PostController {
	@Autowired
	private PostService postService;
	@Autowired
	private JwtService jwt;
	@GetMapping("/getfollowingpost/{userid}")
	public List<PostEntity> getFollowing(@PathVariable("userid") int userId){
		return postService.getFollowing(userId);
	}
	@GetMapping("/trending")
	public List<PostEntity> trending(){
		return postService.trending();
	}
	
	//public List<PostEntity> region(int postid,double lati,double longi);
	@GetMapping("/postbyuser/{userid}")
	public List<PostEntity> postByUser(@PathVariable("userid") int userId){
		return postService.postByUser(userId);
	}
	@PostMapping("/createpost")
	public PostEntity createPost(@RequestBody PostEntity post) {
		return postService.createPost(post);
	}
	@PutMapping("/delete")
	public String deletePost(@RequestParam int postid,@RequestParam int userId) {
		return postService.deletePost(postid, userId);
	}
	@PutMapping("/editpost")
	public PostEntity editTitle(@RequestParam int postId,@RequestParam int userId,@RequestParam String title) {
		return postService.editTitle(postId, userId, title);
	}
	//public List<PostEntity> legacy();
	@GetMapping("/feed")
	public List<PostEntity> getFeed(@RequestHeader String header){
		String email=jwt.extractUsername(header);
		return postService.feed(email);
		
	}
	@PostMapping("/saved/{postid}")
	public boolean saved( @PathVariable("postid") int postid,@RequestHeader String token) {
		String username=jwt.extractUsername(token);
		return postService.saved(postid, username);
		
	}
	
	@GetMapping("/search")
	public List<PostEntity> search(@RequestParam("q") String query, @RequestHeader("Authorization") String token) {
		String username = jwt.extractUsername(token.substring(7));
		return postService.search(query, username);
	}
}
