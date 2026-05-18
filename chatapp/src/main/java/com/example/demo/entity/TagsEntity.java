package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name="tags")
public class TagsEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer tagId;
	@jakarta.persistence.Column(unique = true)
	private String tags;
	@ManyToMany
	private List<PostEntity> posts;
	private Integer postCount;
	private Integer recentUse;
	@OneToMany
	private List<PostEntity> recentPosts;
	public Integer getTagId() {
		return tagId;
	}
	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}
	public List<PostEntity> getPosts() {
		return posts;
	}
	public void setPosts(List<PostEntity> posts) {
		this.posts = posts;
	}
	public Integer getPostCount() {
		return postCount;
	}
	public void setPostCount(Integer postCount) {
		this.postCount = postCount;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public Integer getRecentUse() {
		return recentUse;
	}
	public void setRecentUse(Integer recentUse) {
		this.recentUse = recentUse;
	}
	public List<PostEntity> getRecentPosts() {
		return recentPosts;
	}
	public void setRecentPosts(List<PostEntity> recentPosts) {
		this.recentPosts = recentPosts;
	}
	
	
	
	

}
