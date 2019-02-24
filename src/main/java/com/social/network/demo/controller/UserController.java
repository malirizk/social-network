package com.social.network.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.social.network.demo.dto.FollowRqDTO;
import com.social.network.demo.model.User;
import com.social.network.demo.service.UserService;

@RepositoryRestController
@RequestMapping
public class UserController {

	@Autowired
	UserService userService;
	
	@PostMapping("/users/{userId}/follow")
	public ResponseEntity<?> followUser(@PathVariable("userId") String userId, @RequestBody FollowRqDTO followRqDTO) {
		User user = userService.followUser(userId, followRqDTO.getFollower());
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@GetMapping("users/{userId}/followers")
	public ResponseEntity<?> findFollowers(@PathVariable("userId") String userId) {
		return new ResponseEntity<List<User>>(userService.findFollowers(userId), HttpStatus.OK);
	}
}
