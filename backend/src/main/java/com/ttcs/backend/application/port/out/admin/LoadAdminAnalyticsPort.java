package com.ttcs.backend.application.port.out.admin;

public interface LoadAdminAnalyticsPort {

    AdminAnalyticsReport loadOverview(AdminAnalyticsReportQuery query);
}
