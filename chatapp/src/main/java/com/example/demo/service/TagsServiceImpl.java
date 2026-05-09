package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;
import com.example.demo.repository.TagsRepo;

@Service
public class TagsServiceImpl implements TagsService {
	@Autowired
	TagsRepo tagRepo;

	@Override
	public List<TagsEntity> tags(String t) {
		return tagRepo.getAllTags(t);
	}

	@Override
	@Transactional
	public void refresh() {
		List<TagsEntity> tag = tagRepo.findAll();
		for (TagsEntity t : tag) {
			boolean flag = true;
			List<PostEntity> q = t.getRecentPosts();
			while (q.size() > 0 && flag) {
				LocalDateTime curr = LocalDateTime.now();
				PostEntity post = q.get(0);
				LocalDateTime postTime = post.getCreatedAt();
				long daydiff = ChronoUnit.HOURS.between(postTime, curr);
				if (daydiff >= 24) {
					q.remove(0);
				} else {
					flag = false;
				}
			}
			t.setRecentPosts(q);
			tagRepo.save(t);
		}
	}

	@Override
	public List<TagsEntity> getTrending() {
		List<TagsEntity> tag = tagRepo.findAll();
		Collections.sort(tag, (a, b) -> b.getRecentPosts().size() - a.getRecentPosts().size());
		return tag;
	}

	@Override
	public List<PostEntity> getPostsByTag(String tag) {
		TagsEntity tags = tagRepo.findByTags(tag);
		if (tags != null && tags.getPosts() != null) {
			List<PostEntity> posts = tags.getPosts();
			Collections.sort(posts, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
			return posts;
		}
		return Collections.emptyList();
	}

}
