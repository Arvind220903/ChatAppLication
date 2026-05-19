package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Likes;
import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.LikesRepo;
import com.example.demo.repository.NotificationRepo;
import com.example.demo.repository.PostRepo;
import com.example.demo.repository.TagsRepo;
import com.example.demo.repository.UserRepo;

@Service
public class LikesServiceImpl implements LikesService {
	@Autowired
	private LikesRepo likesRepo;
	@Autowired
	private PostRepo postRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private NotificationRepo notificationRepo;
	@Autowired
	private TagsRepo tagsRepo;
	@Override
	@Transactional
	public String likePost(String email, int postId) {
		UserEntity user = userRepo.findByUserEmail(email);
		Likes like = likesRepo.findByUserIdAndPostId(user.getUserId(), postId);
		if (like == null) {
			like = new Likes();
			PostEntity post=postRepo.findByPostId(postId);
			post.setLikeCount(post.getLikeCount()+1);
			post.setLikedByUser(true);
			postRepo.save(post);
			like.setUserId(user.getUserId());
			like.setPostId(postId);
			likesRepo.save(like);
			NotificationEntity notification=notificationRepo.findByTitleAndUserIdAndPostIdAndSender(
					user.getUserName()+" Liked Your Post",post.getUser(),post.getPostId(),user.getUserId());
			if(notification==null || (notification.getUserId()!=post.getUser() || notification.getPostId()!=post.getPostId())) {
				notification=new NotificationEntity();
			
			notification.setTitle(user.getUserName()+" Liked Your Post");
			notification.setUserId(post.getUser());
			notification.setPostId(post.getPostId());
			notification.setSender(user.getUserId());
			
			UserEntity user1=userRepo.findByUserId(post.getUser());
			user1.setUnseenNoti((user1.getUnseenNoti()==null?0:user1.getUnseenNoti())+1);
			
			List<String> tags=post.getTags();
			
			Set<TagsEntity> likeTags=user1.getLikeTags();
			if(likeTags==null)likeTags=new HashSet<>();
			if (tags != null && !tags.isEmpty()) {
				likeTags.addAll(tagsRepo.findBatchByTags(tags));
			}
			
			user1.setLikeTags(likeTags);
			userRepo.save(user1);
			
			}
			if(notification!=null)notification.setPostId(post.getPostId());
			notificationRepo.save(notification);
			
			return "liked";
		} else {
			// Must clear the join table (users_likes) BEFORE deleting from likes
			PostEntity post=postRepo.findByPostId(postId);
			post.setLikeCount(post.getLikeCount()-1);
			post.setLikedByUser(false);
			postRepo.save(post);
			likesRepo.deleteFromJoinTable(like.getLikeId());
			likesRepo.deleteByUserIdAndPostId(user.getUserId(), postId);
			return "unliked";
		}
	}

	@Override
	public int likeCount(int postId) {
		PostEntity post = postRepo.findByPostId(postId);
		if (post == null) return 0;
		return post.getLikeCount();
	}

	@Override
	@Transactional(readOnly = true)
	public List<PostEntity> getByLikes(String username, int pageNumber, int pageSize) {
		Pageable p = PageRequest.of(pageNumber, pageSize);
		UserEntity user = userRepo.findByUserEmail(username);
		if (user == null) return new ArrayList<>();

		List<PostEntity> ans = postRepo.findLikedPosts(user.getUserId(), p);

		Set<PostEntity> saved=user.getSaved();
		Set<PostEntity> savedPosts=new HashSet<>();
		if(saved!=null)savedPosts.addAll(saved);
		
		Set<Integer> userIds = new HashSet<>();
		if (ans != null) {
			for (PostEntity post : ans) {
				if (post != null && post.getUser() != null) {
					userIds.add(post.getUser());
				}
			}
		}
		List<UserEntity> users = userRepo.findAllById(userIds);
		Map<Integer, String> userMap = new HashMap<>();
		for (UserEntity u : users) {
			userMap.put(u.getUserId(), u.getUserName());
		}

		if (ans != null) {
			for (PostEntity post : ans) {
				if (post != null) {
					if (post.getUserName() == null) {
						post.setUserName(userMap.getOrDefault(post.getUser(), "Unknown"));
					}
					post.setLikedByUser(true);
					if(savedPosts.contains(post))post.setSaveByuser(true);
				}
			}
		}

		return ans;
	}
}
