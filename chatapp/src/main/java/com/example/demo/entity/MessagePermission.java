package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="messagepermission")
public class MessagePermission {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer MessagePermissionId;
	private Integer userId1;
	private Integer userId2;
	private String status;
	public Integer getMessagePermissionId() {
		return MessagePermissionId;
	}
	public void setMessagePermissionId(Integer messagePermissionId) {
		MessagePermissionId = messagePermissionId;
	}
	public Integer getUserId1() {
		return userId1;
	}
	public void setUserId1(Integer userId1) {
		this.userId1 = userId1;
	}
	public Integer getUserId2() {
		return userId2;
	}
	public void setUserId2(Integer userId2) {
		this.userId2 = userId2;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	

}
