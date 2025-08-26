package com.dimetyd.bot.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.ShortageJobData;
import com.dimetyd.bot.process.PromotionalAgreementbackupDownload;
import com.dimetyd.bot.process.FundingAgreementDetails;
import com.dimetyd.bot.process.PromotionDetailsFetch;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class Promotional_OB_Jobs_prior {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private LoginVendorCentral loginObj;

    @Autowired
    private PromotionDetailsFetch promodetails;

    @Autowired
    private FundingAgreementDetails fundingagreement;
    
    @Autowired
    private PromotionalAgreementbackupDownload promotionalStage1Page;

    static Page page;
    BrowserContext context;
    private int counter = 0;
    HashMap<String, String> loginHashMap = new HashMap<>();

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("Promotional_OB_Bot_prior")) {
            logger.info("PromotionInvoice & agreement details Bot Started...");

            List<String> list = new ArrayList<>();
            list.add("--disable-webauthn");
            list.add("--disable-features=PasswordlessLogin");

            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

            Path downloadPath = Paths.get("C:\\Playwright File");
            
            while (true) {
                processNotFeaturedPromotionalAgreement(browser);
                processPromotionalAgreementCheck(browser, downloadPath);
            }
        }
    }

    private void processNotFeaturedPromotionalAgreement(Browser browser) {
        String sql = "SELECT * FROM (SELECT cr.id AS id , c.id AS requestId, c.RequestType, c.vendorId, v.vendorName,"
                + " cr.retry, cr.agreementId, cr.startDt, cr.endDt, c.createdBy FROM CBRequest c "
                + " JOIN CBAgreementRequestDetails cr ON (c.id=cr.requestId) "
                + " JOIN Vendor v ON (v.id = c.vendorId) "
                + " WHERE cr.status = 'PENDING' AND v.isPaused = 'N' AND c.vendorId IN('VN20220719040302','VN20221208081028','VN20230714051352','VN20220724050238','VN20220724050241','VN20230807090225','VN20220923060116','VN20220603050219','VN20220603050223','VN20230906050234','VN20241104051351','VN20230314045255','VN20241015014142','VN20221103030135','VN20230906050234','VN20220724050241','VN20220923060116','VN20221103030135','VN20230314045255')"
                + " AND RequestType IN ('NOTFEATURED_PROMOTIONAL_AGREEMENT','KILLED_PROMOTIONAL_AGREEMENT') AND cr.machineName IS NULL "
                + " ORDER BY v.master_Crd_Id LIMIT 100) AS z ORDER BY z.id LIMIT 1";
        //VN20241015014142
        List<ShortageJobData> jobList = fetchJobData(sql);
        if (jobList.isEmpty()) {
            logger.info("No Data Found for Not Featured Promotional Agreement");
            return;
        }

        executeJob(jobList.get(0), browser);
    }

    private void processPromotionalAgreementCheck(Browser browser, Path downloadPath) {
        String sql = "SELECT cr.id AS id, c.id AS requestId, c.RequestType, c.vendorId, v.vendorName, cr.retry, "
                + "cr.agreementId, cr.startDt, cr.endDt, c.createdBy FROM CBRequest c "
                + "JOIN CBAgreementRequestDetails cr ON c.id = cr.requestId "
                + "JOIN Vendor v ON v.id = c.vendorId "
                + "WHERE cr.status = 'PENDING' AND v.isPaused = 'N' AND c.vendorId IN('VN20220719040302','VN20221208081028','VN20230714051352','VN20220724050238','VN20220724050241','VN20230807090225','VN20220923060116','VN20220603050219','VN20220603050223','VN20230906050234','VN20241104051351','VN20230314045255','VN20241015014142','VN20221103030135','VN20230906050234','VN20220724050241','VN20220923060116','VN20221103030135','VN20230314045255')"
                + "AND c.RequestType IN ('PROMOTIONAL_AGREEMENT_CHECK') ORDER BY v.master_Crd_Id LIMIT 1";
        
        List<ShortageJobData> jobList = fetchJobData(sql);
        if (jobList.isEmpty()) {
            logger.info("No Data Found for Promotional Agreement Check");
            return;
        }

        ShortageJobData job = jobList.get(0);
        executePromotionalAgreementCheck(job, browser, downloadPath);
    }

    private List<ShortageJobData> fetchJobData(String sql) {
        return jdbcTemplate.query(sql, new RowMapper<ShortageJobData>() {
            @Override
            public ShortageJobData mapRow(ResultSet rs, int rowNum) throws SQLException {
                ShortageJobData res = new ShortageJobData();
                res.setVendorId(rs.getString("vendorId"));
                res.setVendorName(rs.getString("vendorName"));
                res.setAgreementId(rs.getString("agreementId"));
                res.setRequestId(rs.getLong("requestId"));
                res.setId(rs.getLong("id"));
                res.setRequestType(rs.getString("RequestType"));
                res.setStartDate(rs.getString("startDt"));
                res.setEndDate(rs.getString("endDt"));
                res.setCreatedBy(rs.getInt("createdBy"));
                return res;
            }
        });
    }

    private void executeJob(ShortageJobData job, Browser browser) {
        counter++;
        Pair<Page, Boolean> loginStatus = loginObj.loginProcess(job.getVendorId(), page, job.getVendorName(), loginHashMap, counter, browser, context);
        page = loginStatus.getLeft();
        if (!loginStatus.getRight()) return;
        
        String updateSQL = "UPDATE CBRequest SET `requestStatus` = 'INPROGRESS', processedStartDate = now()  WHERE id = '"+job.getRequestId()+"'";
        jdbcTemplate.update(updateSQL);
        
        boolean isJobCompleted = promodetails.getPromotionDetails(page, job.getRequestId(), job.getStartDate().toString(), job.getEndDate().toString(), job.getVendorId(), job.getVendorName(), browser);
        updateJobStatus(job.getId(), isJobCompleted);
    }

    private void executePromotionalAgreementCheck(ShortageJobData job, Browser browser, Path downloadPath) {
        counter++;
        Pair<Page, Boolean> loginStatus = loginObj.loginProcess(job.getVendorId(), page, job.getVendorName(), loginHashMap, counter, browser, context);
        page = loginStatus.getLeft();
        if (!loginStatus.getRight()) return;
        
        String updateSQL = "UPDATE CBRequest SET `requestStatus` = 'INPROGRESS', processedStartDate = now()  WHERE id = '"+job.getRequestId()+"'";
        jdbcTemplate.update(updateSQL);
        
        boolean status = promotionalStage1Page.processPage(page, job.getId(), job.getAgreementId(), job.getVendorId(), job.getVendorName(), job.getRequestId(), downloadPath);
        updateJobStatus(job.getId(), status);
    }

    private void updateJobStatus(Long id, boolean isCompleted) {
        String status = isCompleted ? "COMPLETED" : "ERROR";
        jdbcTemplate.execute("UPDATE CBAgreementRequestDetails SET status='" + status + "' WHERE id='" + id + "'");
    }
}
