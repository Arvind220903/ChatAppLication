package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CommentEntity;
import com.example.demo.service.CommentService;

@CrossOrigin(origins = "http://localhost:4201")
@RestController
@RequestMapping("/comments")
public class CommentController {
	@Autowired
	private CommentService commentService;

	@PostMapping("/addcomment")
	public List<CommentEntity> addComment(@RequestBody CommentEntity comment) {
		return commentService.addComment(comment);
	}

	@PutMapping("/editcomment")
	public String editComment(@RequestParam int commentId, @RequestParam int userid, @RequestParam String comment) {
		return commentService.editComment(commentId, userid, comment);
	}

	@PutMapping("/deletecomment")
	public String deleteComment(@RequestParam int commentId, @RequestParam int userid) {
		return commentService.deleteComment(commentId, userid);
	}
}
