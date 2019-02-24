package com.social.network.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.social.network.demo.dao.UserRepository;
import com.social.network.demo.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	public User followUser(String fromUser, String toUser) throws IllegalArgumentException {
		if (StringUtils.isEmpty(fromUser) || StringUtils.isEmpty(toUser)) {
			throw new IllegalArgumentException();
		}
		Optional<User> from = userRepository.findById(UUID.fromString(fromUser));
		Optional<User> to = userRepository.findById(UUID.fromString(toUser));
		
		if (!from.isPresent() || !to.isPresent()) {
			throw new IllegalArgumentException();
		}
		from.get().getFollowers().add(to.get());
		return userRepository.save(from.get());
	}
	
	public List<User> findFollowers(String userId) throws IllegalArgumentException {
		if (StringUtils.isEmpty(userId)) {
			throw new IllegalArgumentException();
		}
		
		Optional<User> from = userRepository.findById(UUID.fromString(userId));
		if (!from.isPresent()) {
			throw new IllegalArgumentException();
		}
		
		return userRepository.findByfollowers(from.get());
	}
}
