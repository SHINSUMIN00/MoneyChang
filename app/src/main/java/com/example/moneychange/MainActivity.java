package com.example.moneychange;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrofit 인스턴스 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.koreaexim.go.kr/site/program/financial/exchangeJSON?authkey=cs9TkrLU83gx3G06kcYdh3thFotBE4NA&searchdate=20240406&data=AP01") // 실제 API의 Base URL로 교체하세요.
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExchangeRateService service = retrofit.create(ExchangeRateService.class);

        // Firestore 인스턴스 초기화
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore에서 데이터 읽기 및 환율 적용
        fetchCurrencyAndConvert(db, service, "1000jpn", "JPY");
        fetchCurrencyAndConvert(db, service, "10000vtn", "VND");
        fetchCurrencyAndConvert(db, service, "5eur", "EUR");
    }

    private void fetchCurrencyAndConvert(FirebaseFirestore db, ExchangeRateService service, String field, String currencyCode) {
        db.collection("money").document("5s2JLMcIzivtRoFp0xMm")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double amount = documentSnapshot.getDouble(field);
                        if (amount != null) {
                            // 환율 API 호출 및 결과 처리
                            service.getExchangeRate(currencyCode).enqueue(new Callback<ExchangeRateResponse>() {
                                @Override
                                public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                                    if (response.isSuccessful()) {
                                        ExchangeRateResponse responseBody = response.body();
                                        if (responseBody != null) {
                                            double rate = responseBody.getRate();
                                            double convertedAmount = amount * rate;
                                            Log.d("CurrencyConversion", field + " to KRW: " + convertedAmount);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                                    Log.e("CurrencyConversion", "Error fetching exchange rate for " + currencyCode, t);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreData", "Error fetching currency data for " + field, e));
    }
}
