package com.example.demo.entity;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="post")
public class PostEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer postId;
	private String title;
	@OneToMany(
		    mappedBy = "postId",
		    cascade = CascadeType.ALL,
		    orphanRemoval = true
		)
		private List<Likes> likes;
	@OneToMany
	private List<CommentEntity> comments;
	
	private Integer user;
	private double latitude;
	private double longitude;
	@CreationTimestamp
	private Date createdAt;
	private Integer likeCount = 0;
	private String userName;
	
	@Transient
	private boolean isLikedByUser;
	@Transient
	private boolean isSaveByuser=false;
	@ManyToMany
	private List<TagsEntity> tags;
	
	public boolean getisLikedByUser() {
		return isLikedByUser;
	}
	public void setLikedByUser(boolean isLikedByUser) {
		this.isLikedByUser = isLikedByUser;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Likes> getLikes() {
		return likes;
	}
	public void setLikes(List<Likes> likes) {
		this.likes = likes;
	}
	public List<CommentEntity> getComments() {
		return comments;
	}
	public void setComments(List<CommentEntity> comments) {
		this.comments = comments;
	}
	public Integer getUser() {
		return user;
	}
	public void setUser(Integer user) {
		this.user = user;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Integer getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public boolean isSaveByuser() {
		return isSaveByuser;
	}
	public void setSaveByuser(boolean isSaveByuser) {
		this.isSaveByuser = isSaveByuser;
	}
	public boolean isLikedByUser() {
		return isLikedByUser;
	}
	public List<TagsEntity> getTags() {
		return tags;
	}
	public void setTags(List<TagsEntity> tags) {
		this.tags = tags;
	}
	
	
	
	
	
	
	
	
}
