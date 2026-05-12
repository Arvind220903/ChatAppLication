package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.PostRepo;
import com.example.demo.repository.TagsRepo;
import com.example.demo.repository.UserRepo;

@Service
public class PostServiceImpl implements PostService {
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private PostRepo postRepo;
	@Autowired

	private TagsRepo tagsRepo;

	@Override
	public List<PostEntity> getFollowing(int userId) {
		UserEntity user = userRepo.findByUserId(userId);
		if (user == null || user.getFollowing() == null || user.getFollowing().isEmpty()) {
			return new ArrayList<>();
		}

		Pageable topTwenty = PageRequest.of(0, 20);
		return postRepo.findByUserInOrderByPostIdDesc(user.getFollowing(), topTwenty);
	}

	@Override
	public List<PostEntity> trending() {
		// Filter posts created within the last 10 days
		LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);

		Pageable topFifteen = PageRequest.of(0, 50);
		List<PostEntity> posts = postRepo.findByCreatedAtAfterOrderByLikeCountDesc(tenDaysAgo, topFifteen);
		posts.forEach(p -> {
			if (p.getUserName() == null) {
				UserEntity u = userRepo.findByUserId(p.getUser());
				if (u != null)
					p.setUserName(u.getUserName());
			}
		});
		return posts;
	}

	@Override
	public List<PostEntity> region(int postid, double lati, double longi) {
		System.out.println("Searching region for Lat: " + lati + ", Lng: " + longi);
		
		double radiusKm = 60.0; // 60km search radius
		double latDelta = radiusKm / 111.0;
		// Dynamically adjust longitude delta based on latitude curvature
		double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lati)));

		List<PostEntity> posts = postRepo.findByLatitudeBetweenAndLongitudeBetween(
				lati - latDelta, lati + latDelta, 
				longi - lonDelta, longi + lonDelta
		);

		// Calculate true distance for each post
		posts.forEach(post -> {
			double dist = haversine(lati, longi, post.getLatitude(), post.getLongitude());
			post.setDistance(dist);
		});

		// Ensure posts strictly fall within the circular radius (trim the bounding box corners)
		posts.removeIf(post -> post.getDistance() > radiusKm);

		// Exclude the originating post from the results if applicable
//		if (postid > 0) {
//			posts.removeIf(post -> post.getPostId() == postid);
//		}

		// Sort from nearest to farthest
		posts.sort((p1, p2) -> Double.compare(p1.getDistance(), p2.getDistance()));

		return posts;
	}

	private double haversine(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double a = Math.pow(Math.sin(dLat / 2), 2) +
				   Math.pow(Math.sin(dLon / 2), 2) *
				   Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.pow(a, 0.5),Math.pow(1-a, 0.5));
		return 6371 * c; // Earth's radius in KM
	}

	@Override
	public List<PostEntity> postByUser(int userId) {
		UserEntity user = userRepo.findByUserId(userId);
		if (user != null && user.getPosts() != null) {
			return user.getPosts();
		}
		return new ArrayList<>();
	}

	@Override
	public PostEntity createPost(PostEntity post, String email) {
		UserEntity user = userRepo.findByUserEmail(email);
		post.setUserName(user.getUserName());
		post.setUser(user.getUserId());

		// Save post first to get an ID and avoid TransientPropertyValueException
		PostEntity savedPost = postRepo.save(post);

		if (savedPost.getTags() != null) {
			for (String tag : savedPost.getTags()) {
				int i = 0;
				while(i<tag.length() && tag.charAt(i)=='#')i++;
				tag=tag.substring(i);
				TagsEntity tags = tagsRepo.findByTags(tag);
				
				if (tags == null) {
					tags = new TagsEntity();
					tags.setTags(tag);
					tags.setRecentPosts(new ArrayList<>());
					tags.setPosts(new ArrayList<>());
					tags.setRecentUse(0);
				}

				List<PostEntity> posts = tags.getPosts();
				posts.add(savedPost);
				tags.setPosts(posts);
				tags.setRecentUse(tags.getRecentUse() + 1);

				List<PostEntity> recent = tags.getRecentPosts();
				recent.add(savedPost);
				tags.setRecentPosts(recent);

				tagsRepo.save(tags);
			}
		}

		user.getPosts().add(savedPost);
		userRepo.save(user);

		return savedPost;
	}

	@Override
	public String deletePost(int postid, int userId) {
		PostEntity post = postRepo.findByPostId(postid);
		if (post != null && post.getUser().equals(userId)) {
			UserEntity user = userRepo.findByUserId(userId);
			List<PostEntity> posts = user.getPosts();
			posts.remove(post);
			user.setPosts(posts);
			userRepo.save(user);
			postRepo.delete(post);
			return "deleted";
		}
		return "wrong User";
	}

	@Override
	public PostEntity editTitle(int postid, int userId, String title) {
		PostEntity post = postRepo.findByPostId(postid);
		if (post != null && post.getUser().equals(userId)) {
			post.setTitle(title);
			return postRepo.save(post);
		}
		return null;
	}

	@Override
	public List<PostEntity> legacy() {
		return null;
	}

	@Override
	public List<PostEntity> feed(String email) {
		int userId = userRepo.findByUserEmail(email).getUserId();
		UserEntity user = userRepo.findByUserId(userId);
		List<PostEntity> trending = trending();
		List<PostEntity> following = getFollowing(userId);
		
		java.util.Set<PostEntity> mixedSet = new java.util.LinkedHashSet<>();

		int tIndex = 0;
		int fIndex = 0;
		List<PostEntity> seen=user.getSeenPost();
		Queue<PostEntity> sPosts=new LinkedList<>();

		while ( fIndex < following.size() && !seen.contains(following.get(fIndex))) {
			if (fIndex < following.size()) {
				
					sPosts.add(following.get(fIndex));
					seen.add(following.get(fIndex));
					
					
					
				fIndex++;
				}
			
		}
		user.setSeenPost(seen);
		userRepo.save(user);
//		while (tIndex < trending.size() || fIndex < following.size()) {
//			if (fIndex < following.size()) {
//				if(!seen.contains(following.get(fIndex))) {
//					mixedSet.add(following.get(fIndex));
//					
//					
//					
//					}
//				fIndex++;
//				}
//			
//			if (fIndex < following.size()) {
//				mixedSet.add(following.get(fIndex++));
//			}
//			if (tIndex < trending.size()) {
//				mixedSet.add(trending.get(tIndex++));
//			}
//		}
		Set<PostEntity> postSet=new HashSet<>();
		
		for(TagsEntity t : user.getLikeTags()) {
			postSet.addAll(t.getPosts());
		}
		for(TagsEntity t:user.getCommentedTags()) {
			postSet.addAll(t.getPosts());
		}
		
		List<PostEntity> result = new ArrayList<>();
		for(PostEntity p1:postSet)result.add(p1);
		result.forEach(p -> {
			if (p.getUserName() == null) {
				UserEntity u = userRepo.findByUserId(p.getUser());
				if (u != null)
					p.setUserName(u.getUserName());
			}
		});
		Collections.sort(result, (a, b) -> {
			if (a.getLikeCount() != b.getLikeCount()) {
				return Integer.compare(b.getLikeCount(), a.getLikeCount());
			}
			return b.getCreatedAt().compareTo(a.getCreatedAt());
		});
		while(!sPosts.isEmpty())result.add(0,sPosts.poll());
		Set<PostEntity> set=new HashSet<>();
		set.addAll(trending());
		for(PostEntity p:set)result.add(p);

		return result;
	}

	public List<PostEntity> search(String keyword, String email) {
		int userId = userRepo.findByUserEmail(email).getUserId();
		List<PostEntity> results = postRepo.findByTitleContainingIgnoreCaseOrUserNameContainingIgnoreCase(keyword,
				keyword);
		results.forEach(p -> {
			if (p.getUserName() == null) {
				UserEntity u = userRepo.findByUserId(p.getUser());
				if (u != null)
					p.setUserName(u.getUserName());
			}
		});
		return results;
	}

	@Override
	public String saved(int postId, String email) {
		PostEntity post = postRepo.findByPostId(postId);
		UserEntity user = userRepo.findByUserEmail(email);
		if (user.getSaved().size() > 0 && user.getSaved().contains(post)) {
			user.getSaved().remove(post);
			userRepo.save(user);
			post.setSaveByuser(false);
			postRepo.save(post);
			return "unsaved";
		} else {
			user.getSaved().add(post);
			userRepo.save(user);
			post.setSaveByuser(true);
			postRepo.save(post);
			return "saved";
		}
	}

	@Override
	public List<PostEntity> likePosts(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PostEntity> savedPosts(String username) {
		UserEntity user = userRepo.findByUserEmail(username);
		List<PostEntity> save = user.getSaved();

		return save;
	}

}
