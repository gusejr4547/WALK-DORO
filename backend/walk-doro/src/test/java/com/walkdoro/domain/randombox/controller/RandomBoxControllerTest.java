package com.walkdoro.domain.randombox.controller;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.randombox.dto.RandomBoxDrawRequest;
import com.walkdoro.domain.randombox.service.RandomBoxService;
import com.walkdoro.domain.randombox.type.RandomBoxType;
import com.walkdoro.global.auth.dto.UserAdapter;
import com.walkdoro.global.error.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.core.MethodParameter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RandomBoxControllerTest {

    private MockMvc mockMvc;
    private RandomBoxService randomBoxService;

    @BeforeEach
    void setUp() {
        randomBoxService = Mockito.mock(RandomBoxService.class);

        HandlerMethodArgumentResolver userAdapterResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserAdapter.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return new UserAdapter("1", "ROLE_USER");
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new RandomBoxController(randomBoxService))
                .setCustomArgumentResolvers(userAdapterResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/random-boxes/draw 정상 요청 시 200 OK 와 리스트 반환")
    void drawBox_Success() throws Exception {
        // given
        Item dummyItem = Item.builder().name("Sample Item").grade(ItemGrade.COMMON).category(ItemCategory.FACE).build();

        given(randomBoxService.drawBox(any(), any(RandomBoxType.class), anyInt()))
                .willReturn(List.of(dummyItem));

        String requestBody = "{\"type\":\"BASIC\",\"quantity\":1}";

        // when & then
        mockMvc.perform(post("/api/v1/random-boxes/draw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Sample Item"))
                .andExpect(jsonPath("$.items[0].grade").value("COMMON"));
    }

    @Test
    @DisplayName("잔액 부족 시 400 Bad Request 반환")
    void drawBox_NotEnoughPoints() throws Exception {
        // given
        given(randomBoxService.drawBox(any(), any(RandomBoxType.class), anyInt()))
                .willThrow(new com.walkdoro.global.error.exception.BusinessException(
                        com.walkdoro.global.error.ErrorCode.NOT_ENOUGH_POINTS));

        String requestBody = "{\"type\":\"BASIC\",\"quantity\":1}";

        // when & then
        mockMvc.perform(post("/api/v1/random-boxes/draw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 RandomBoxType 요청 시 400 Bad Request와 INVALID_INPUT_VALUE 반환")
    void drawBox_InvalidBoxType() throws Exception {
        // given: "BASIX" is an invalid RandomBoxType enum value
        String requestBody = "{\"type\":\"BASIX\",\"quantity\":1}";

        // when & then
        mockMvc.perform(post("/api/v1/random-boxes/draw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
