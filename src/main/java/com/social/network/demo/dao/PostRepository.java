package com.social.network.demo.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.social.network.demo.model.Post;
import com.social.network.demo.model.User;

@RepositoryRestResource(collectionResourceRel = "posts", path = "posts")
public interface PostRepository extends PagingAndSortingRepository<Post, UUID> {

	@Query("Select p from Post p where p.owner.id=:userId order by p.createdDate desc")
	public List<Post> findByUserId(UUID userId);

	@Query("Select DISTINCT p from Post p where p.owner=:user or p.owner IN :#{#user.followers} order by p.createdDate desc")
	public List<Post> findByUserIdAndFollower(@Param("user") User user);
}
