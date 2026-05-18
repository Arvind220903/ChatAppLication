package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.CommentEntity;
import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.CommentRepo;
import com.example.demo.repository.NotificationRepo;
import com.example.demo.repository.PostRepo;
import com.example.demo.repository.TagsRepo;
import com.example.demo.repository.UserRepo;

@Service
public class CommentServiceImpl implements CommentService{
	@Autowired
	private CommentRepo commentRepo;
	@Autowired
	private PostRepo postRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private NotificationRepo notificationRepo;
	@Autowired
	private TagsRepo tagsRepo;

	@Override
	public List<CommentEntity> addComment(CommentEntity comment) {
		PostEntity post = postRepo.findByPostId(comment.getPostId());
		if (post == null) return null;
		
		UserEntity user = userRepo.findByUserId(comment.getUserId());
		if (user == null) return null;
		
		comment.setUsername(user.getUserName());
		CommentEntity saved = commentRepo.save(comment);
		post.getComments().add(saved);
		postRepo.save(post);
		
		NotificationEntity notification=notificationRepo.findByTitleAndUserIdAndPostIdAndSender(
				user.getUserName()+" commented on your post",post.getUser(),post.getPostId(),user.getUserId());
		if(notification==null ) {
			notification=new NotificationEntity();
		
		notification.setTitle(user.getUserName()+" commented on your post");
		notification.setUserId(post.getUser());
		notification.setPostId(post.getPostId());
		notification.setSender(user.getUserId());
		
		UserEntity user1 = (user.getUserId() == post.getUser()) ? user : userRepo.findByUserId(post.getUser());
		user1.setUnseenNoti((user1.getUnseenNoti()==null?0:user1.getUnseenNoti())+1);
		
		List<String> tags=post.getTags();
		
		Set<TagsEntity> commentedTags=user1.getCommentedTags();
		if(commentedTags==null)commentedTags=new HashSet<>();
		if (tags != null && !tags.isEmpty()) {
			commentedTags.addAll(tagsRepo.findBatchByTags(tags));
		}
		
		user1.setCommentedTags(commentedTags);
		userRepo.save(user1);
		
		
		}
		if(notification!=null)notification.setPostId(post.getPostId());
		notificationRepo.save(notification);
		return post.getComments();
	}

	@Override
	public String editComment(int commentId, int userid, String change) {
		CommentEntity comment = commentRepo.findByCommentId(commentId);
		if (comment != null && comment.getUserId().equals(userid)) {
			comment.setComment(change);
			commentRepo.save(comment);
			return "Success";
		}
		return "Failed";
	}

	@Override
	public String deleteComment(int commentId, int userid) {
		CommentEntity comment = commentRepo.findByCommentId(commentId);
		if (comment != null && comment.getUserId().equals(userid)) {
			commentRepo.delete(comment);
			return "Deleted";
		}
		return "Failed";
	}

	@Override
	public List<PostEntity> getPostsByComments(String username) {
		// Query the comment table directly by userId — the UserEntity.comment
		// join table is never populated so getComment() always returns empty.
		UserEntity user = userRepo.findByUserEmail(username);
		if (user == null) return new ArrayList<>();

		List<CommentEntity> comments = commentRepo.findByUserId(user.getUserId());
		List<PostEntity> ans = new ArrayList<>();
		Set<Integer> seen = new LinkedHashSet<>();
		Set<Integer> set=new HashSet<>();
		for(CommentEntity c:comments)set.add(c.getPostId());
		
		ans.addAll(postRepo.findAllById(set));
		Collections.reverse(ans);                   // newest first
		return ans;
	}
}
