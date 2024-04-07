package com.example.moneychange;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExchangeRateService {
    @GET("path/to/exchange/rate/api") // 실제 환율 정보 API의 URL로 교체해야 합니다.
    Call<ExchangeRateResponse> getExchangeRate(@Query("currency") String currency);
}
