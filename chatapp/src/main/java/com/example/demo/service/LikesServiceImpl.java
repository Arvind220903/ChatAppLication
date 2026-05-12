package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
			
			List<TagsEntity> likeTags=user1.getLikeTags();
			if(likeTags==null)likeTags=new ArrayList<>();
			for(String s : tags) {
				TagsEntity t1=tagsRepo.findByTags(s);
				if(t1!=null)likeTags.add(t1);
			}
			user1.setLikeTags(likeTags);
			userRepo.save(user1);
			
			}
			
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
	public List<PostEntity> getByLikes(String username) {
		UserEntity user = userRepo.findByUserEmail(username);
		if (user == null) return new ArrayList<>();
		
		List<Likes> likes = likesRepo.findByUserId(user.getUserId());
		List<PostEntity> ans = new ArrayList<>();
		for(Likes like : likes) {
			PostEntity post = postRepo.findByPostId(like.getPostId());
			if (post != null) {
				
				ans.add(post);
			}
		}
		return ans;
	}
}
