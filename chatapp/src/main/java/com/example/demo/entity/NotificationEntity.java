package com.example.demo.entity;

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
		private Date createdAt;

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

		public Date getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
}
