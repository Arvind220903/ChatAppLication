package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
			t.setRecentUse(q.size());
			tagRepo.save(t);
		}
	}

	@Override
	public List<TagsEntity> getTrending(int pageNumber,int pageSize) {
		Pageable p=PageRequest.of(pageNumber,pageSize,Sort.by(Sort.Direction.DESC,"recentUse"));
		Page<TagsEntity> tag = tagRepo.findAll(p);
		List<TagsEntity> tags=new ArrayList<>();
		for(TagsEntity t:tag) {
			if(t.getRecentUse()>0)tags.add(t);
		}
		
		return tags;
	}

	@Override
	public List<PostEntity> getPostsByTag(String tag) {
		int i = 0;
		while(i<tag.length() && tag.charAt(i)=='#')i++;
		tag=tag.substring(i);
		TagsEntity tags = tagRepo.findFirstByTags(tag);
		if (tags != null && tags.getPosts() != null) {
			List<PostEntity> posts = tags.getPosts();
			Collections.sort(posts, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
			return posts;
		}
		return Collections.emptyList();
	}

}
