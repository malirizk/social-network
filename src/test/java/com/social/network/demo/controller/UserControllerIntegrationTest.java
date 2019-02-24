package com.social.network.demo.controller;

import static org.hamcrest.Matchers.hasSize;
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

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void userFollowAnotherUser() throws UnsupportedEncodingException, Exception {
		String userId = createUser("Mohamed", "Ali");
		String userId2 = createUser("Tom", "Hanks");

		this.mockMvc
				.perform(post(FOLLOW_RESOURCE, userId).content("{ \"follower\" : \"" + userId2 + "\" }")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.followers", hasSize(1)))
				.andDo(document("index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
						pathParameters(parameterWithName("userId").description("User identifier.")),
						requestFields(fieldWithPath("follower").description("Follower user Id.")),
						responseFields(fieldWithPath("id").description("User identifier."),
								fieldWithPath("firstName").description("User's first name."),
								fieldWithPath("lastName").description("User's last name."),
								fieldWithPath("createdDate").description("User's creation time."),
								fieldWithPath("lastModifiedDate").description("User's last update time."),
								fieldWithPath("posts").description("User posted messages").ignored(),
								fieldWithPath("followers[].id").description("User identifier."),
								fieldWithPath("followers[].firstName").description("User's first name."),
								fieldWithPath("followers[].lastName").description("User's last name."),
								fieldWithPath("followers[].createdDate").description("User's creation time."),
								fieldWithPath("followers[].lastModifiedDate").description("User's last update time."),
								fieldWithPath("followers[].posts").ignored(),
								fieldWithPath("followers[].followers").ignored(),
								fieldWithPath("followers[].following").ignored(),
								fieldWithPath("followers[].following[].id").ignored(),
								fieldWithPath("followers[].following[].firstName").ignored(),
								fieldWithPath("followers[].following[].lastName").ignored(),
								fieldWithPath("followers[].following[].createdDate").ignored(),
								fieldWithPath("followers[].following[].lastModifiedDate").ignored(),
								fieldWithPath("following").ignored()),

						responseHeaders(headerWithName("Content-Type")
								.description("The Content-Type of the payload, e.g. `application/json`"))));

		this.mockMvc.perform(get(USERS_RESOURCE).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.users", hasSize(2)));

		this.mockMvc.perform(get(FOLLOWERS_RESOURCE, userId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
	}
}
