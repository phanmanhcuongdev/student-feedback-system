package com.ttcs.backend.application.port.in.admin;

public interface GetAdminAnalyticsOverviewUseCase {

    AdminAnalyticsOverviewResult getOverview(AdminAnalyticsOverviewQuery query);
}
