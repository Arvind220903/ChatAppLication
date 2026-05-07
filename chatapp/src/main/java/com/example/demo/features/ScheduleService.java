package com.example.demo.features;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.service.TagsService;

@Component
public class ScheduleService {
	@Autowired
	TagsService tag;
		@Scheduled(fixedRate=1000*60*30)
		public void refresh() {
			tag.refresh();
		}
		
}
