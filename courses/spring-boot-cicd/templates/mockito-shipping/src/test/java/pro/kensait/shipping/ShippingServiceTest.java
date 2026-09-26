package pro.kensait.shipping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * 配送のテスト
 */
@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {
    @Mock
    private ShippingRateClient rateClient;
    @Mock
    private ShippingRepository repository;
    private ShippingService service;
    private final Instant now = Instant.parse("2026-04-01T00:00:00Z");

    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        service = new ShippingService(rateClient, repository, Clock.fixed(now, ZoneOffset.UTC));
    }

    // 「固定時刻を用いた割引済み注文の保存」の検証
    @Test
    void savesDiscountedOrderWithFixedTime() {
        when(rateClient.quote("1000001", 2000)).thenReturn(4000);
        ShippingOrder actual = service.order("1000001", 2000, Membership.GOLD);
        ArgumentCaptor<ShippingOrder> captor = ArgumentCaptor.forClass(ShippingOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new ShippingOrder("1000001", 2000, 3600, now));
        assertThat(actual).isEqualTo(captor.getValue());
        verify(rateClient).quote("1000001", 2000);
    }

    // 「料金サービス障害時の未保存」の検証
    @Test
    void doesNotSaveWhenRateServiceFails() {
        when(rateClient.quote("1000001", 2000)).thenThrow(new IllegalStateException("料金取得失敗"));
        assertThatThrownBy(() -> service.order("1000001", 2000, Membership.GOLD))
                .isInstanceOf(IllegalStateException.class).hasMessage("料金取得失敗");
        verifyNoInteractions(repository);
    }

    // 「依存先呼び出し前の不正注文の拒否」の検証
    @Test
    void rejectsInvalidOrderBeforeCallingDependencies() {
        assertThatThrownBy(() -> service.order("invalid", 2000, Membership.GOLD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.order("1000001", 0, Membership.GOLD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.order("1000001", 2000, null))
                .isInstanceOf(NullPointerException.class);
        verifyNoInteractions(rateClient, repository);
    }
}
