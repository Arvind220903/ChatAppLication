package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PostEntity;
import com.example.demo.service.PostService;

@RestController
@RequestMapping("/posts")
public class PostController {
	@Autowired
	private PostService postService;

	@GetMapping("/getfollowingpost/{userid}")
	public List<PostEntity> getFollowing(@PathVariable("userid") int userId) {
		return postService.getFollowing(userId);
	}

	@GetMapping("/trending")
	public List<PostEntity> trending() {
		return postService.trending();
	}

	@GetMapping("/postbyuser/{userid}")
	public List<PostEntity> postByUser(@PathVariable("userid") int userId) {
		return postService.postByUser(userId);
	}

	@GetMapping("/region")
	public List<PostEntity> getByRegion(@RequestParam double lat, @RequestParam double lng) {
		return postService.region(0, lat, lng);
	}

	@PostMapping("/createpost")
	public PostEntity createPost(@RequestBody PostEntity post, Principal principal) {
		if (principal == null)
			return null;
		return postService.createPost(post, principal.getName());
	}

	@PutMapping("/delete")
	public String deletePost(@RequestParam int postid, @RequestParam int userId) {
		return postService.deletePost(postid, userId);
	}

	@PutMapping("/editpost")
	public PostEntity editTitle(@RequestParam int postId, @RequestParam int userId, @RequestParam String title) {
		return postService.editTitle(postId, userId, title);
	}

	@GetMapping("/feed")
	public List<PostEntity> getFeed(Principal principal) {
		if (principal == null)
			return null;
		return postService.feed(principal.getName());
	}

	@PostMapping("/saved/{postid}")
	public String saved(@PathVariable("postid") int postid, Principal principal) {
		if (principal == null)
			return "User not authenticated";
		return postService.saved(postid, principal.getName());
	}

	@GetMapping("/search")
	public List<PostEntity> search(@RequestParam("q") String query, Principal principal) {
		if (principal == null)
			return null;
		return postService.search(query, principal.getName());
	}

	@GetMapping("/savedposts")
	public List<PostEntity> savedPosts(Principal principal) {
		if (principal == null)
			return null;
		return postService.savedPosts(principal.getName());
	}
}
