package com.social.network.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.social.network.demo.model.Post;
import com.social.network.demo.model.User;

public class PostControllerIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void createPostWithNewUser() throws Exception {
		Post post = new Post();
		post.setMessage("Hello World!!!");
		User owner = new User();
		owner.setFirstName("Mohamed");
		owner.setLastName("Ali");
		post.setOwner(owner);

		this.mockMvc
				.perform(post(POSTS_RESOURCE).content(this.mapper.writeValueAsString(post))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("message")
										.description("Descriptive post message with max 140 characters."),
								fieldWithPath("owner.firstName").description("Post owner's first name."),
								fieldWithPath("owner.lastName").description("Post owner's last name.")),
						responseFields(postResponseFieldDescriptors(false)),
						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/json`"))));

		this.mockMvc.perform(get(USERS_RESOURCE).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.users", hasSize(1)));
	}

	@Test
	public void createPostForUser() throws Exception {
		String userId = createUser("Mohamed", "Ali");
		createPost(userId, "Hello World!!!");

		this.mockMvc.perform(get(USERS_RESOURCE).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.users", hasSize(1)));
	}

	@Test
	public void createPostWithTagedUser() throws Exception {
		String userId = createUser("Mohamed", "Ali");
		String user2Id = createUser("Tom", "Hanks");
		createPost(userId, "Hello World!!!", user2Id);
		this.mockMvc.perform(get(USERS_RESOURCE).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.users", hasSize(2)));

		this.mockMvc.perform(get(WALL_RESOURCE, userId).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].message", is("Hello World!!!")))
				.andExpect(jsonPath("$[0].taggedUsers", hasSize(1)))
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						pathParameters(parameterWithName("userId").description("User identifier.")),
						responseFields(postResponseArrayFieldDescriptors(true)),
						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/json`"))));
	}

	@Test
	public void loadUserWall() throws UnsupportedEncodingException, Exception {
		String userId = createUser("Mohamed", "Ali");
		createPost(userId, "Hello World!!!");
		createPost(userId, "Hello Again!!!");

		this.mockMvc.perform(get(WALL_RESOURCE, userId).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].message", is("Hello Again!!!")))
				.andExpect(jsonPath("$[1].message", is("Hello World!!!")))
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						pathParameters(parameterWithName("userId").description("User identifier.")),
						responseFields(postResponseArrayFieldDescriptors(false)),
						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/json`"))));
	}

	@Test
	public void loadTimelineWithFollowers() throws UnsupportedEncodingException, Exception {
		String userId = createUser("Mohamed", "Ali");
		String userId2 = createUser("Tom", "Hanks");

		this.mockMvc
				.perform(post(FOLLOW_RESOURCE, userId).content("{ \"follower\" : \"" + userId2 + "\" }")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.followers", hasSize(1)));

		// Mohamed create a post
		createPost(userId, "Hello World!!!");

		// Tom create a post
		createPost(userId2, "Hello Again!!!");

		// Load Mohamed timeline
		this.mockMvc.perform(get(TIMELINE_RESOURCE, userId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].message", is("Hello Again!!!")))
				.andExpect(jsonPath("$[1].message", is("Hello World!!!")))
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						pathParameters(parameterWithName("userId").description("User identifier.")),
						responseFields(postResponseArrayFieldDescriptors(false)),
						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/json`"))));
	}
}
