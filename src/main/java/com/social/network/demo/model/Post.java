package com.social.network.demo.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
@Entity
public class Post extends AbstractEntity {

	@NotBlank
	@Length(max = 140)
	private String message;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@NotNull
	@JsonIgnoreProperties(value = { "posts", "followers", "following" })
	private User owner;

	@OneToMany
	@JsonIgnoreProperties(value = { "posts", "followers", "following" })
	private List<User> taggedUsers;
}
