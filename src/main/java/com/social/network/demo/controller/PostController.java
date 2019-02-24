package com.social.network.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.social.network.demo.model.Post;
import com.social.network.demo.service.PostService;

@RepositoryRestController
@RequestMapping
public class PostController {

	@Autowired
	private PostService postService;

	@PostMapping(path = "/posts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Post> createPostWithNewUser(@RequestBody Post post) {
		post = postService.createPost(post);
		return new ResponseEntity<Post>(post, HttpStatus.CREATED);
	}

	@GetMapping(path = "/users/{userId}/wall", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAllPostsByUser(@PathVariable String userId) {
		List<Post> posts = postService.loadUserWall(userId);
		return ResponseEntity.ok(posts);
	}

	@GetMapping(path = "/users/{userId}/timeline", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAllPostsByUserAndFollowers(@PathVariable String userId) {
		List<Post> posts = postService.loadUserTimeLine(userId);
		return ResponseEntity.ok(posts);
	}
}
