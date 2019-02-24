package com.social.network.demo.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.social.network.demo.model.Post;
import com.social.network.demo.model.User;

@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

	@Autowired
	protected ObjectMapper mapper;

	@LocalServerPort
	protected int localPort;

	@Autowired
	private WebApplicationContext context;

	protected MockMvc mockMvc;

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-docs");

	public static final String USERS_RESOURCE = "/users";
	public static final String FOLLOWERS_RESOURCE = "/users/{userId}/followers";
	public static final String POSTS_RESOURCE = "/posts";
	public static final String WALL_RESOURCE = "/users/{userId}/wall";
	public static final String FOLLOW_RESOURCE = "/users/{userId}/follow";
	public static final String TIMELINE_RESOURCE = "/users/{userId}/timeline";

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).alwaysDo(document("{method-name}/{step}/",
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.build();
	}

	protected String createUser(String firstName, String lastName) throws Exception, UnsupportedEncodingException {
		User user = new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);

		MvcResult result = this.mockMvc
				.perform(post(USERS_RESOURCE).content(this.mapper.writeValueAsString(user))
						.accept("application/hal+json"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.firstName", is(user.getFirstName())))
				.andExpect(jsonPath("$.lastName", is(user.getLastName())))
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						responseFields(fieldWithPath("firstName").description("User's first name."),
								fieldWithPath("lastName").description("User's last name."),
								fieldWithPath("createdDate").description("User's creation time."),
								fieldWithPath("lastModifiedDate").description("User's last update time."),
								subsectionWithPath("_links").description("Links to other resources")),
						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/hal+json`"))))
				.andReturn();

		String selfUrl = result.getResponse().getHeaders("location").get(0);
		String userId = selfUrl.substring(selfUrl.lastIndexOf("/") + 1);

		return userId;
	}

	protected void createPost(String userId, String message, String... taggedUserIds) throws Exception {
		assertNotNull(userId);

		Post post = new Post();
		post.setMessage(message);
		User owner = new User();
		owner.setId(UUID.fromString(userId));
		post.setOwner(owner);
		if (taggedUserIds != null && taggedUserIds.length > 0) {
			List<User> tagedUsers = new ArrayList<>();
			for (String tagUserId : taggedUserIds) {
				User tagUser = new User();
				tagUser.setId(UUID.fromString(tagUserId));
				tagedUsers.add(tagUser);
			}
			post.setTaggedUsers(tagedUsers);
		}

		this.mockMvc
				.perform(post(POSTS_RESOURCE).content(this.mapper.writeValueAsString(post))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						requestFields(postRequestFieldDescriptors(taggedUserIds != null && taggedUserIds.length > 0)),
						responseFields(postResponseFieldDescriptors(taggedUserIds != null && taggedUserIds.length > 0)),
						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/json`"))));
	}

	protected List<FieldDescriptor> postResponseFieldDescriptors(boolean istaggedUser) {
		List<FieldDescriptor> fieldDescriptors = new ArrayList();
		fieldDescriptors.add(fieldWithPath("id").description("Post public id."));
		fieldDescriptors.add(fieldWithPath("message").description("Descriptive post message with max 140 characters."));
		fieldDescriptors.add(fieldWithPath("owner.id").description("Post owner identifier."));
		fieldDescriptors.add(fieldWithPath("owner.firstName").description("Post owner's first name."));
		fieldDescriptors.add(fieldWithPath("owner.lastName").description("Post owner's last name."));
		fieldDescriptors.add(fieldWithPath("owner.createdDate").description("Post owner's creation time."));
		fieldDescriptors.add(fieldWithPath("owner.lastModifiedDate").description("Post owner's last update time."));
		if (istaggedUser) {
			fieldDescriptors.add(
					fieldWithPath("taggedUsers").description("Array of tagged users within this post.").optional());
			fieldDescriptors.add(fieldWithPath("taggedUsers[].id").description("Post owner identifier."));
		}
		fieldDescriptors.add(fieldWithPath("createdDate").description("Post creation time."));
		fieldDescriptors.add(fieldWithPath("lastModifiedDate").description("Post last update time."));
		return fieldDescriptors;
	}

	protected List<FieldDescriptor> postRequestFieldDescriptors(boolean istaggedUser) {
		List<FieldDescriptor> fieldDescriptors = new ArrayList();
		fieldDescriptors.add(fieldWithPath("message").description("Descriptive post message with max 140 characters."));
		fieldDescriptors.add(fieldWithPath("owner.id").description("Owner public id."));
		if (istaggedUser) {
			fieldDescriptors.add(fieldWithPath("taggedUsers[].id").description("Tagged user public id."));
		}
		return fieldDescriptors;
	}

	protected List<FieldDescriptor> postResponseArrayFieldDescriptors(boolean istaggedUser) {
		List<FieldDescriptor> fieldDescriptors = new ArrayList();
		fieldDescriptors.add(fieldWithPath("[].id").description("Post public id."));
		fieldDescriptors
				.add(fieldWithPath("[].message").description("Descriptive post message with max 140 characters."));
		fieldDescriptors.add(fieldWithPath("[].owner.id").description("Post owner identifier."));
		fieldDescriptors.add(fieldWithPath("[].owner.firstName").description("Post owner's first name."));
		fieldDescriptors.add(fieldWithPath("[].owner.lastName").description("Post owner's last name."));
		fieldDescriptors.add(fieldWithPath("[].owner.createdDate").description("Post owner's creation time."));
		fieldDescriptors.add(fieldWithPath("[].owner.lastModifiedDate").description("Post owner's last update time."));
		if (istaggedUser) {
			fieldDescriptors.add(
					fieldWithPath("[].taggedUsers").description("Array of tagged users within this post.").optional());
			fieldDescriptors.add(fieldWithPath("[].taggedUsers[].id").description("Post owner identifier."));
			fieldDescriptors.add(fieldWithPath("[].taggedUsers[].firstName").description("Tagged user's first name."));
			fieldDescriptors.add(fieldWithPath("[].taggedUsers[].lastName").description("Tagged user's last name."));
			fieldDescriptors
					.add(fieldWithPath("[].taggedUsers[].createdDate").description("Tagged user's creation time."));
			fieldDescriptors.add(
					fieldWithPath("[].taggedUsers[].lastModifiedDate").description("Tagged user's last update time."));
		} else {
			fieldDescriptors.add(
					fieldWithPath("[].taggedUsers").description("Array of tagged users within this post.").optional());
		}
		fieldDescriptors.add(fieldWithPath("[].createdDate").description("Post creation time."));
		fieldDescriptors.add(fieldWithPath("[].lastModifiedDate").description("Post last update time."));
		return fieldDescriptors;
	}

	protected static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
					.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true).writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
