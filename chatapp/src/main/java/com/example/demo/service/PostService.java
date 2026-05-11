package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.PostEntity;

@Service
public interface PostService {
	public List<PostEntity> getFollowing(int userId);

	public List<PostEntity> region(int postid, double lati, double longi);

	public List<PostEntity> postByUser(int userId);

	public String deletePost(int postid, int userId);

	public PostEntity editTitle(int postId, int userId, String title);

	public List<PostEntity> legacy();

	public List<PostEntity> feed(String email);

	public String saved(int postId, String email);

	public List<PostEntity> likePosts(String email);

	public List<PostEntity> search(String keyword, String email);

	public List<PostEntity> savedPosts(String username);

	public PostEntity createPost(PostEntity post, String username);

	public List<PostEntity> trending();
}
