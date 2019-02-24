package com.social.network.demo.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
@Entity
public class User extends AbstractEntity {

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	private List<Post> posts;

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "USER_FOLLOWER", joinColumns = { @JoinColumn(name = "USER_ID") }, inverseJoinColumns = {
			@JoinColumn(name = "FOLLOWER_ID") })
	private List<User> followers = new ArrayList<User>();

	@JsonIgnoreProperties(value = { "posts", "followers", "following" })
	@ManyToMany(mappedBy = "followers")
	private List<User> following = new ArrayList<User>();
}
