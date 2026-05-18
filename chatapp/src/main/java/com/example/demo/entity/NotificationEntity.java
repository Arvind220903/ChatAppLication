package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="notifications")
public class NotificationEntity {
		@Id
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		private Integer notificationId;
		private String title;
		private Integer userId;
		
		@CreationTimestamp
		private LocalDateTime createdAt;
		private boolean seen=false;
		private Integer postId;
		private Integer sender;
		private String followEmail;

		public Integer getNotificationId() {
			return notificationId;
		}

		public void setNotificationId(Integer notificationId) {
			this.notificationId = notificationId;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public boolean isSeen() {
			return seen;
		}

		public void setSeen(boolean seen) {
			this.seen = seen;
		}

		public Integer getPostId() {
			return postId;
		}

		
		public Integer getSender() {
			return sender;
		}

		public void setSender(Integer sender) {
			this.sender = sender;
		}

		public void setPostId(Integer postId) {
			this.postId = postId;
		}

		public String getFollowEmail() {
			return followEmail;
		}

		public void setFollowEmail(String followEmail) {
			this.followEmail = followEmail;
		}
		
		
		
}
