package com.fonbnk.app;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TransactionHolderApi {

    @GET("transactions")
    Call<List<Transaction>> getTransations();
}
