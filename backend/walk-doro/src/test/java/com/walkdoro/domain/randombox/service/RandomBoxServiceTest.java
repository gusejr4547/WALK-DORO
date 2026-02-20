package com.walkdoro.domain.randombox.service;

import com.walkdoro.domain.randombox.type.RandomBoxType;
import com.walkdoro.domain.user.Role;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.inventory.Inventory;
import com.walkdoro.domain.inventory.repository.InventoryRepository;
import com.walkdoro.domain.item.repository.ItemRepository;

import com.walkdoro.global.util.RandomGenerator;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RandomBoxServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private RandomGenerator randomGenerator;

    @InjectMocks
    private RandomBoxService randomBoxService;

    @Test
    @DisplayName("사용자를 찾을 수 없으면 예외를 발생해야 한다")
    void drawBox_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> randomBoxService.drawBox(1L, RandomBoxType.BASIC, 1))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("해당 등급의 아이템이 없으면 예외를 발생해야 한다")
    void drawBox_ItemNotFound() {
        // given
        User user = User.builder().name("tester").email("test@net.com").role(Role.USER).build();
        user.addPoint(10L); // enough points

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(randomGenerator.nextDouble()).willReturn(0.1); // COMMON

        // return empty list
        given(itemRepository.findAllByGrade(ItemGrade.COMMON)).willReturn(List.of());

        // then
        assertThatThrownBy(() -> randomBoxService.drawBox(1L, RandomBoxType.BASIC, 1))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("drawBox 는 사용자의 포인트가 부족하면 예외를 발생시켜야 한다")
    void drawBox_NotEnoughPoints() {
        // given
        User user = User.builder().name("tester").email("test@net.com").role(Role.USER).build();
        user.update("tester"); // assuming 0 points initially

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> randomBoxService.drawBox(1L, RandomBoxType.BASIC, 1))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_ENOUGH_POINTS);
    }

    @Test
    @DisplayName("단일 뽑기(quantity=1) 시 지정된 타입의 가격만큼 포인트가 차감되어야 한다")
    void drawBox_DeductPoints() {
        // given
        User user = User.builder().name("tester").email("test@net.com").role(Role.USER).build();
        user.addPoint(10L); // enough points

        Item dummyItem = Item.builder().name("Sample Item").grade(ItemGrade.RARE).category(ItemCategory.FACE).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(randomGenerator.nextDouble()).willReturn(0.1); // 0.1 for PREMIUM -> RARE
        given(itemRepository.findAllByGrade(ItemGrade.RARE)).willReturn(List.of(dummyItem));
        given(inventoryRepository.findByUserAndItem(user, dummyItem)).willReturn(Optional.empty());

        // when
        randomBoxService.drawBox(1L, RandomBoxType.PREMIUM, 1); // costs 5

        // then
        assertThat(user.getPoint()).isEqualTo(5L);
    }

    @Test
    @DisplayName("단일 뽑기 후 결과가 Inventory 에 올바르게 반영되어야 한다")
    void drawBox_UpdateInventory() {
        // given
        User user = User.builder().name("tester").email("test@net.com").role(Role.USER).build();
        user.addPoint(10L);

        Item dummyItem = Item.builder().name("Sample Item").grade(ItemGrade.COMMON).category(ItemCategory.FACE).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(randomGenerator.nextDouble()).willReturn(0.1);
        given(itemRepository.findAllByGrade(ItemGrade.COMMON)).willReturn(List.of(dummyItem));
        given(inventoryRepository.findByUserAndItem(user, dummyItem)).willReturn(Optional.empty());

        // when
        List<Item> result = randomBoxService.drawBox(1L, RandomBoxType.BASIC, 1);

        // then
        assertThat(result).containsExactly(dummyItem);

        org.mockito.ArgumentCaptor<Inventory> captor = org.mockito.ArgumentCaptor.forClass(Inventory.class);
        org.mockito.Mockito.verify(inventoryRepository).save(captor.capture());

        Inventory saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getItem()).isEqualTo(dummyItem);
        assertThat(saved.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("일괄 뽑기(bulk draw) 시 지정된 수량만큼 아이템이 뽑히고 올바른 포인트가 차감되어야 한다")
    void drawBox_BulkDraw() {
        // given
        User user = User.builder().name("tester").email("test@net.com").role(Role.USER).build();
        user.addPoint(50L);

        Item commonItem = Item.builder().name("Common").grade(ItemGrade.COMMON).category(ItemCategory.FACE).build();
        Item rareItem = Item.builder().name("Rare").grade(ItemGrade.RARE).category(ItemCategory.HEADGEAR).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // 2 BASIC draws: random values 0.1 (COMMON) and 0.9 (EPIC - wait I'll use 0.6
        // for RARE)
        given(randomGenerator.nextDouble()).willReturn(0.1, 0.0, 0.6, 0.0); // 0.1->COMMON, 0.0->index0; 0.6->RARE,
                                                                            // 0.0->index0
        given(itemRepository.findAllByGrade(ItemGrade.COMMON)).willReturn(List.of(commonItem));
        given(itemRepository.findAllByGrade(ItemGrade.RARE)).willReturn(List.of(rareItem));

        given(inventoryRepository.findByUserAndItem(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .willReturn(Optional.empty());

        // when
        List<Item> result = randomBoxService.drawBox(1L, RandomBoxType.BASIC, 2); // 2 draws, 1 pt each

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(commonItem, rareItem);

        assertThat(user.getPoint()).isEqualTo(48L); // 50 - 2 = 48

        org.mockito.Mockito.verify(inventoryRepository, org.mockito.Mockito.times(2))
                .save(org.mockito.ArgumentMatchers.any(Inventory.class));
    }
}
