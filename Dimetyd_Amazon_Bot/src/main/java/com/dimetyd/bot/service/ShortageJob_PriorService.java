package com.dimetyd.bot.service;

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
import com.dimetyd.bot.process.ShortagePage;
import com.dimetyd.bot.util.LoginVendorCentral;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;

@Service
public class ShortageJob_PriorService {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	LoginVendorCentral loginObj;
	@Autowired
	ShortagePage shortagePage;

	static Page page;
	BrowserContext context;
	HashMap<String, String> loginHashMap = new HashMap<String, String>();
	private int counter = 0;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("Shortage_Job_prior")) {

			logger.info("Shortage bot started.");

			List<String> list = new ArrayList<>();
			list.add("--disable-webauthn");
			list.add("--disable-features=PasswordlessLogin");

			Playwright playwright = Playwright.create();

			Browser browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setHeadless(false).setArgs(list));

			logger.info("Page Started...");

			while (true) {

				StringBuilder sql = new StringBuilder();
				
				
				sql.append(
				"select * from (SELECT cr.id AS id , c.id AS requestId,c.RequestType, c.vendorId, v.vendorName,cr.`agreementId`,cr.`startDt`,cr.`endDt`, c.createdBy FROM CBRequest c JOIN `CBAgreementRequestDetails` cr ON (c.id=cr.requestId) JOIN Vendor v ON (v.id = c.vendorId)  WHERE cr.`status` = 'PENDING' AND c.vendorId IN('VN19062020160922','VN20220517013340','VN20241129071105','VN20230509125651','VN20241018062754','VN20230321055232','VN20230511040205','VN20231108052550','VN20230719090213','VN20240702040616','VN20240910115358','VN07022024022400','VN20230620102357','VN20240605050108','VN20220217024245','VN29012024112603','VN20240314120832','VN20240725110955','VN20240725110956','VN20240423123914','VN20230504050225','VN20221109031641','VN20231118041603','VN20220803050249','VN20221102030126','VN20240725110958','VN20220901050109','VN20231222110321','VN20231109052510','VN20220601062653','VN20240725110959','VN20211215120148','VN20240725111000','VN20231102084026','VN20231220065842','VN20240522061412','VN20240521054759','VN20230914050205','VN20240417042613','VN20230523090300','VN20240816050629','VN20220618042102','VN29052020125321','VN20230728060217','VN20220411052536','VN20231113072204','VN20230530092254','VN20230530102906','VN20240806050619','VN10062020150411','VN20240124021119','VN20240725111002','VN02032022112111','VN20231116040937','VN20241224053024','VN20241108082247','VN20240411033128','VN20230418060214','VN20230610101258','VN20022024022600','VN20240524090221','VN20230324040155','VN20221012060157','VN20240725111005','VN20230413060212','VN20240315060737','VN20220427044828','VN20221212060159','VN20230614050358','VN20230125054544','VN20231024120629','VN20230630052243','VN20230414061600','VN19012024032400','VN29012024112604','VN20230503030148','VN20230404040209','VN20221027034214','VN20240418102303','VN20240725111007','VN20221108020151','VN20240403041909','VN20231024120701','VN20240328044816','VN20240725111011','VN20240725111008','VN20240725111009','VN20221006060228','VN20230210043839','VN20230907100221','VN20230907100243','VN20230210044446','VN20240710042032','VN20230524020346','VN20240506013449','VN20240702050754','VN20240925040624','VN01172022122801','VN20241123121839','VN20230610081010','VN20221018013633','VN20240710042033','VN20230429064713','VN20240726121144','VN20240508072819','VN20240912081635','VN20240531032639','VN20241125072502','VN20230105064119','VN20240726121148','VN20220708050100','VN20231116043631','VN02022024022400','VN29012024112600','VN21022024022700','VN20220708050100','VN20230518022337','VN20240702050754','VN20230531022459','VN20230307070524','VN20240710042032','VN20240710042033','VN20230323065334','VN20240506013449','VN20230918070234','VN20230531052300','VN20240726121144','VN20230531060241','VN20230531031433','VN20240710042026','VN20220818050119','VN20220610014121','VN20240522061412','VN20240423123914','VN20230610101258','VN20230530092254','VN20241108082247','VN20230727081404','VN20220507063417','VN20230330045547','VN20230530102906','VN20230714070340','VN20240521054759','VN20220622060119','VN20240725111006','VN20240725111005','VN20240725110958','VN20240725111000','VN20240725111002','VN20240725110956','VN20240725111008','VN20240725110955','VN20240725111009','VN20240725111011','VN20241011042425','VN20240522061409','VN20240625035424','VN20231108052550','VN20241011042424','VN20022024022600','VN20240508072819','VN20240806050619','VN20230614050358','VN20221212060159','VN20240725110959','VN20240726121148','VN20240520070216','VN20240725111007','VN20230531030224','VN20220427044828','VN20221103030135','VN20220517013340','VN20240816050629','VN20230105064119','VN20231030120450','VN20231021015028','VN20230822121932','VN20231102092053','VN20231021015054','VN20231021014938','VN20221027034637','VN20220521095249','VN20230928100301','VN20231103122008','VN20240925040624','VN20240105041921','VN20231024120906','VN20230606110444','VN19012024042400','VN20230610123414','VN20230429064713','VN20231024120558','VN20231113101354','VN20220317063907','VN20230502040227','VN20240822100603','VN20230405100158','VN20240328044816','VN20221231050201','VN20221117031755','VN20230522060450','VN20221231050218','VN20221029030150','VN20221231050225','VN20221231050211','VN20230120080153','VN20230714062335','VN20230426035300','VN20221130040442','VN20230121070137','VN20221118030131','VN20230620102357','VN20221231050154','VN20231102084026','VN20230511040205','VN20241218011027','VN20250502013015','VN20230630052243','VN20240605050108','VN20241123121839','VN20230418060214')  AND v.`isPaused` = 'N' "
				+ " AND `RequestType` IN ('SHORTAGE_RECONCILIATION')   ) as z ORDER BY RAND() limit 1");
				List<ShortageJobData> catlogList = this.jdbcTemplate.query(sql.toString(),
						new RowMapper<ShortageJobData>() {
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
						}, new Object[] {});

				if (catlogList.size() == 0) {
					logger.info("No Data Found in Stage 1");
					String stage = "Stage1";

					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					counter = counter + 1;
					// insertCBBotTransactionDetails(processName, transName, vendorName);
					jdbcTemplate.execute(
							"update CBRequest set requestStatus='INPROGRESS',processedStartDate =NOW() where id='"
									+ catlogList.get(0).getRequestId() + "'");
					jdbcTemplate.execute(
							"update CBAgreementRequestDetails set status='INPROGRESS',processedStartDate =NOW() "
									+ "where id='" + catlogList.get(0).getId() + "'");

					Pair<Page, Boolean> loginStatus = loginObj.loginProcess(catlogList.get(0).getVendorId(), page,
							catlogList.get(0).getVendorName(), loginHashMap, counter, browser, context);
					page = loginStatus.getLeft();

					if (loginStatus.getRight()) {

						boolean status = shortagePage.processPage(page, catlogList.get(0).getId(),
								catlogList.get(0).getVendorId(), catlogList.get(0).getVendorName(),
								catlogList.get(0).getRequestId().toString(), catlogList.get(0).getStartDate(),
								catlogList.get(0).getEndDate(), catlogList.get(0).getCreatedBy(),
								Paths.get("C:\\PalyWrightFile"));

						if (status) {
							logger.info("update CBAgreementRequestDetails set status='COMPLETED' where id='"
									+ catlogList.get(0).getId() + "'");
							jdbcTemplate.execute("update CBAgreementRequestDetails set status='COMPLETED' where id='"
									+ catlogList.get(0).getId() + "'");

						}

						else {
							logger.info("update CBAgreementRequestDetails set status='ERROR' where id='"
									+ catlogList.get(0).getId() + "'");
							jdbcTemplate.execute("update CBAgreementRequestDetails set status='ERROR' where id='"
									+ catlogList.get(0).getId() + "'");

						}

					}

				}

			}

		}

	}

}
