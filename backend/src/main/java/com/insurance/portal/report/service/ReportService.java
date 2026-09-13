package com.insurance.portal.report.service;

import com.insurance.portal.report.dto.AdminReportResponse;
import com.insurance.portal.report.dto.CustomerReportResponse;

public interface ReportService {

    CustomerReportResponse getCustomerReport();

    AdminReportResponse getAdminReport();
}

