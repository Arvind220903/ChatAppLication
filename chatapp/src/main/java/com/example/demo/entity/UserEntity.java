package com.example.demo.entity;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Users")
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	private String userName;
	private String userEmail;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	private String userbio;
	private String userProfile;
	@CreationTimestamp
	private Date createdAt;

	@ElementCollection
	private List<Integer> follower;

	@ElementCollection
	private List<Integer> following;

	@OneToMany
	@JsonIgnore
	private List<PostEntity> posts;

	@OneToMany
	@JsonIgnore
	private List<CommentEntity> comment;

	@OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<Likes> likes;

	@OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<NotificationEntity> notifications;

	@ManyToMany
	@JsonIgnore
	private List<PostEntity> saved;

	private Integer postCount;
	private Integer followerCount;
	private Integer followingCount;

	@OneToMany
	@JsonIgnore
	private List<MessageEntity> messages;

	@ManyToMany
	@JsonIgnore
	private List<UserEntity> chatWith;
	private Integer unseenNoti=0;

	// Getters and Setters
	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public String getUserName() { return userName; }
	public void setUserName(String userName) { this.userName = userName; }
	public String getUserEmail() { return userEmail; }
	public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getUserbio() { return userbio; }
	public void setUserbio(String userbio) { this.userbio = userbio; }
	public String getUserProfile() { return userProfile; }
	public void setUserProfile(String userProfile) { this.userProfile = userProfile; }
	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
	public List<Integer> getFollower() { return follower; }
	public void setFollower(List<Integer> follower) { this.follower = follower; }
	public List<Integer> getFollowing() { return following; }
	public void setFollowing(List<Integer> following) { this.following = following; }
	public List<PostEntity> getPosts() { return posts; }
	public void setPosts(List<PostEntity> posts) { this.posts = posts; }
	public List<CommentEntity> getComment() { return comment; }
	public void setComment(List<CommentEntity> comment) { this.comment = comment; }
	public List<Likes> getLikes() { return likes; }
	public void setLikes(List<Likes> likes) { this.likes = likes; }
	public List<PostEntity> getSaved() { return saved; }
	public void setSaved(List<PostEntity> saved) { this.saved = saved; }
	public List<NotificationEntity> getNotifications() { return notifications; }
	public void setNotifications(List<NotificationEntity> notifications) { this.notifications = notifications; }
	public Integer getPostCount() { return postCount; }
	public void setPostCount(Integer postCount) { this.postCount = postCount; }
	public Integer getFollowerCount() { return followerCount; }
	public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }
	public Integer getFollowingCount() { return followingCount; }
	public void setFollowingCount(Integer followingCount) { this.followingCount = followingCount; }
	public List<MessageEntity> getMessages() { return messages; }
	public void setMessages(List<MessageEntity> messages) { this.messages = messages; }
	public List<UserEntity> getChatWith() { return chatWith; }
	public void setChatWith(List<UserEntity> chatWith) { this.chatWith = chatWith; }
	public Integer getUnseenNoti() {
		return unseenNoti;
	}
	public void setUnseenNoti(Integer unseenNoti) {
		this.unseenNoti = unseenNoti;
	}
	
}
