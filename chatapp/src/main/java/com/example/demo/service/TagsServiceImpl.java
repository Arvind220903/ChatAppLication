package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.TagsEntity;
import com.example.demo.repository.TagsRepo;

@Service
public class TagsServiceImpl implements TagsService{
	@Autowired
	TagsRepo tagRepo;
	@Override
	public List<TagsEntity> tags() {
			List<TagsEntity> tag=tagRepo.findAll();
			Collections.sort(tag,(a,b)->a.getRecentUse()-b.getRecentUse());
		return tag;
	}

	@Override
	public void refresh() {
		List<TagsEntity> tag=tagRepo.findAll();
		for(TagsEntity t:tag) {
			boolean flag=true;
			Queue<PostEntity> q=t.getRecentPosts();
			while(!q.isEmpty() && flag){
				LocalDateTime curr=LocalDateTime.now();
				PostEntity post=q.peek();
				LocalDateTime postTime=post.getCreatedAt();
				long daydiff= ChronoUnit.HOURS.between(postTime,curr);
				if(daydiff>=24) {
					q.poll();
				}else {
					flag=false;
				}
			}
			t.setRecentPosts(q);
		}
		
	}
	@Override
	public List<TagsEntity> getTrending() {
		
		List<TagsEntity> tag=tagRepo.findAll();
		Collections.sort(tag,(a,b)->b.getRecentPosts().size()-a.getRecentPosts().size());
		
		return tag;
	}
	
}
