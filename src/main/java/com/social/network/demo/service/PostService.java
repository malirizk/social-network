package com.social.network.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.social.network.demo.dao.PostRepository;
import com.social.network.demo.dao.UserRepository;
import com.social.network.demo.model.Post;
import com.social.network.demo.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	public Post createPost(Post post) throws IllegalArgumentException {
		if (post == null || post.getOwner() == null) {
			throw new IllegalArgumentException();
		}

		User owner;
		if (post.getOwner().getId() == null) {
			owner = userRepository.save(post.getOwner());
			log.info("Create post for new user : {}", post.getOwner());
		} else {
			Optional<User> user = userRepository.findById(post.getOwner().getId());
			if (!user.isPresent()) {
				log.error("User Id {} is not exist", post.getOwner().getId());
				throw new IllegalArgumentException("Invalid User ID");
			}
			owner = user.get();
		}
		post.setOwner(owner);
		post = postRepository.save(post);
		return post;
	}

	public List<Post> loadUserWall(String userId) throws IllegalArgumentException {
		Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));

		if (!userOptional.isPresent()) {
			throw new IllegalArgumentException("Invalid User");
		}

		return postRepository.findByUserId(userOptional.get().getId());
	}

	public List<Post> loadUserTimeLine(String userId) throws IllegalArgumentException {
		Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));

		if (!userOptional.isPresent()) {
			throw new IllegalArgumentException("Invalid User");
		}

		return postRepository.findByUserIdAndFollower(userOptional.get());
	}
}
