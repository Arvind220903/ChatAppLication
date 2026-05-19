package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Likes;
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
		List<PostEntity> posts = postRepo.findByUserInOrderByPostIdDesc(new ArrayList<>(user.getFollowing()), topTwenty);

		Set<Integer> userIds = new HashSet<>();
		for (PostEntity p : posts) {
			if (p.getUser() != null) {
				userIds.add(p.getUser());
			}
		}
		List<UserEntity> users = userRepo.findAllById(userIds);
		Map<Integer, String> userMap = new HashMap<>();
		for (UserEntity u : users) {
			userMap.put(u.getUserId(), u.getUserName());
		}

		posts.forEach(p -> {
			if (p.getUserName() == null) {
				p.setUserName(userMap.getOrDefault(p.getUser(), "Unknown"));
			}
		});

		return posts;
	}

	@Override
	public List<PostEntity> trending(String email, int pageNumber, int pageSize) {
		UserEntity user = userRepo.findByUserEmail(email);
		LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);

		Set<Integer> likedPost = new HashSet<>();
		Set<Integer> savedPost = new HashSet<>();

		if (user != null) {
			if (user.getLikes() != null) {
				for (Likes l : user.getLikes())
					likedPost.add(l.getPostId());
			}
			if (user.getSaved() != null) {
				for (PostEntity p : user.getSaved())
					savedPost.add(p.getPostId());
			}
		}

		Pageable topFifteen = PageRequest.of(pageNumber, pageSize);
		List<PostEntity> posts = postRepo.findByCreatedAtAfterOrderByLikeCountDesc(tenDaysAgo, topFifteen);

		Set<Integer> userIds = new HashSet<>();
		for (PostEntity pe : posts)
			userIds.add(pe.getUser());
		Set<UserEntity> users = new HashSet<>();
		users.addAll(userRepo.findAllById(userIds));
		Map<Integer, String> map = new HashMap<>();
		for (UserEntity u : users)
			map.put(u.getUserId(), u.getUserName());

		posts.forEach(p -> {
			if (p.getUserName() == null) {
				
					p.setUserName(map.get(p.getUser()));
			}
			if (likedPost.contains(p.getPostId()))
				p.setLikedByUser(true);
			if (savedPost.contains(p.getPostId()))
				p.setSaveByuser(true);

		});
		return posts;
	}

	@Override
	public List<PostEntity> region(int postid, double lati, double longi) {
		System.out.println("Searching region for Lat: " + lati + ", Lng: " + longi);

		double radiusKm = 60.0;
		double latDelta = radiusKm / 111.0;

		double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lati)));

		List<PostEntity> posts = postRepo.findByLatitudeBetweenAndLongitudeBetweenOrderByPostIdDesc(
				lati - latDelta, lati + latDelta,
				longi - lonDelta, longi + lonDelta);

		Set<Integer> userIds = new HashSet<>();
		for (PostEntity pe : posts)
			userIds.add(pe.getUser());
		Set<UserEntity> users = new HashSet<>();
		users.addAll(userRepo.findAllById(userIds));
		Map<Integer, String> map = new HashMap<>();
		for (UserEntity u : users)
			map.put(u.getUserId(), u.getUserName());
		posts.forEach(post -> {
			double dist = haversine(lati, longi, post.getLatitude(), post.getLongitude());
			post.setDistance(dist);

			if (post.getUserName() == null) {

				post.setUserName(map.get(post.getUser()));
			}
		});

		posts.removeIf(post -> post.getDistance() > radiusKm);
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
		double c = 2 * Math.atan2(Math.pow(a, 0.5), Math.pow(1 - a, 0.5));
		return 6371 * c; // Earth's radius in KM
	}

	@Override
	public List<PostEntity> postByUser(int userId, String currentEmail) {
		UserEntity profileUser = userRepo.findByUserId(userId);
		UserEntity currentUser = userRepo.findByUserEmail(currentEmail);

		if (profileUser != null && profileUser.getPosts() != null) {
			List<PostEntity> posts = new ArrayList<>(profileUser.getPosts());

			// Backfill Liked/Saved status
			Set<Integer> likedPostIds = new HashSet<>();
			Set<Integer> savedPostIds = new HashSet<>();
			if (currentUser != null) {
				if (currentUser.getLikes() != null) {
					for (Likes l : currentUser.getLikes())
						likedPostIds.add(l.getPostId());
				}
				if (currentUser.getSaved() != null) {
					for (PostEntity p : currentUser.getSaved())
						savedPostIds.add(p.getPostId());
				}
			}

			for (PostEntity p : posts) {
				p.setLikedByUser(likedPostIds.contains(p.getPostId()));
				p.setSaveByuser(savedPostIds.contains(p.getPostId()));
				if (p.getUserName() == null)
					p.setUserName(profileUser.getUserName());
			}

			Collections.sort(posts, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
			return posts;
		}
		return new ArrayList<>();
	}

	@Override
	@Transactional
	public PostEntity createPost(PostEntity post, String email) {
		UserEntity user = userRepo.findByUserEmail(email);
		post.setUserName(user.getUserName());
		post.setUser(user.getUserId());

		// Save post first to get an ID and avoid TransientPropertyValueException
		PostEntity savedPost = postRepo.save(post);

		if (savedPost.getTags() != null) {
			for (String tag : savedPost.getTags()) {
				int i = 0;
				while (i < tag.length() && tag.charAt(i) == '#')
					i++;
				tag = tag.substring(i);
				TagsEntity tags = tagsRepo.findFirstByTags(tag);

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
		List<PostEntity> posts = new ArrayList<>();
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
	@Transactional
	public List<PostEntity> feed(String email, int pageNumber, int pageSize) {
		UserEntity user = userRepo.findByUserEmail(email);
		if (user == null)
			return new ArrayList<>();
		int userId = user.getUserId();

		Set<PostEntity> pool = new java.util.LinkedHashSet<>();

		List<PostEntity> following = getFollowing(userId);
		Set<PostEntity> seenSet = new HashSet<>(user.getSeenPost());
		for (PostEntity p : following) {
			if (!seenSet.contains(p) && p.getUser() != userId) {
				pool.add(p);
			}
		}

		addInterestPosts(user, pool, userId);

		List<PostEntity> trending = trending(user.getUserEmail(), 0, 50);
		for (PostEntity p : trending)
			if (p.getUser() != userId)
				pool.add(p);

		if (pool.size() < (pageNumber + 1) * pageSize) {
			Pageable fallback = PageRequest.of(0, 50);
			List<PostEntity> allRecent = postRepo.findAllByOrderByPostIdDesc(fallback);
			for (PostEntity p : allRecent) {
				if (p.getUser() != userId)
					pool.add(p);
			}
		}

		List<PostEntity> allPosts = new ArrayList<>(pool);
		int start = pageNumber * pageSize;
		int end = Math.min(start + pageSize, allPosts.size());

		if (start >= allPosts.size())
			return new ArrayList<>();
		List<PostEntity> result = allPosts.subList(start, end);

		for (PostEntity p : result) {
			if (following.contains(p))
				user.getSeenPost().add(p);
		}
		userRepo.save(user);

		Set<Integer> likedPostIds = new HashSet<>();
		if (user.getLikes() != null) {
			for (Likes l : user.getLikes()) {
				likedPostIds.add(l.getPostId());
			}
		}

		Set<Integer> savedPostIds = new HashSet<>();
		if (user.getSaved() != null) {
			for (PostEntity p : user.getSaved()) {
				savedPostIds.add(p.getPostId());
			}
		}
		
		Set<Integer> userIds = new HashSet<>();
		for (PostEntity pe : result)
			userIds.add(pe.getUser());
		Set<UserEntity> users = new HashSet<>();
		users.addAll(userRepo.findAllById(userIds));
		Map<Integer, String> map = new HashMap<>();
		for (UserEntity u : users)
			map.put(u.getUserId(), u.getUserName());

		for (PostEntity p : result) {
			// Set Username
			if (p.getUserName() == null) {
				
					p.setUserName(map.get(p.getUser()));
			}
			// Set Liked status
			p.setLikedByUser(likedPostIds.contains(p.getPostId()));
			// Set Saved status
			p.setSaveByuser(savedPostIds.contains(p.getPostId()));
		}

		return result;
	}

	private void addInterestPosts(UserEntity user, Set<PostEntity> feedSet, int userId) {
		Set<TagsEntity> interests = new HashSet<>();
		if (user.getLikeTags() != null)
			interests.addAll(user.getLikeTags());
		if (user.getCommentedTags() != null)
			interests.addAll(user.getCommentedTags());
		for (TagsEntity tag : interests) {
			List<PostEntity> tagPosts = tag.getPosts();
			if (tagPosts != null) {
				for (PostEntity p : tagPosts)
					if (p.getUser() != userId)
						feedSet.add(p);
			}
		}
	}

	public List<PostEntity> search(String keyword, String email) {
		UserEntity user = userRepo.findByUserEmail(email);
		List<PostEntity> results = postRepo.findByTitleContainingIgnoreCaseOrUserNameContainingIgnoreCase(keyword,
				keyword);

		// Backfill data
		Set<Integer> likedPostIds = new HashSet<>();
		Set<Integer> savedPostIds = new HashSet<>();
		if (user != null) {
			if (user.getLikes() != null) {
				for (Likes l : user.getLikes())
					likedPostIds.add(l.getPostId());
			}
			if (user.getSaved() != null) {
				for (PostEntity p : user.getSaved())
					savedPostIds.add(p.getPostId());
			}
		}

		Set<Integer> userIds = new HashSet<>();
		for (PostEntity pe : results)
			userIds.add(pe.getUser());
		Set<UserEntity> users = new HashSet<>();
		users.addAll(userRepo.findAllById(userIds));
		Map<Integer, String> map = new HashMap<>();
		for (UserEntity u : users)
			map.put(u.getUserId(), u.getUserName());

		results.forEach(p -> {
			if (p.getUserName() == null) {
				
					p.setUserName(map.get(p.getUser()));
			}
			p.setLikedByUser(likedPostIds.contains(p.getPostId()));
			p.setSaveByuser(savedPostIds.contains(p.getPostId()));
		});
		return results;
	}

	@Override
	public String saved(int postId, String email) {
		PostEntity post = postRepo.findByPostId(postId);
		UserEntity user = userRepo.findByUserEmail(email);
		Set<PostEntity> saved = user.getSaved();
		if (saved == null) {
			saved = new HashSet<>();
			user.setSaved(saved);
		}
		if (saved.contains(post)) {
			saved.remove(post);
			userRepo.save(user);
			post.setSaveByuser(false);
			postRepo.save(post);
			return "unsaved";
		} else {
			saved.add(post);
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
		Set<PostEntity> save = user.getSaved();
		if (save == null) return new ArrayList<>();

		return new ArrayList<>(save);
	}

	@Override
	public PostEntity getPostById(int postId) {
		PostEntity p = postRepo.findByPostId(postId);
		if (p != null && p.getUserName() == null) {
			UserEntity u = userRepo.findByUserId(p.getUser());
			if (u != null)
				p.setUserName(u.getUserName());
		}
		return p;
	}

}
