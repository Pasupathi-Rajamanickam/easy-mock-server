package com.pastech;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import io.javalin.Context;
import io.javalin.Javalin;

public class MockServer {
	private static String dataSourceDirectoryExcel = System.getProperty("DataSourceDirectoryExcel");
	private static String port = System.getProperty("ServerPort");
	private static final String SHEET_NAME = "mockdata";

	public static void main(String... s) {
		Javalin app = Javalin.create().start(Integer.parseInt(port));
		app.get("*", ctx -> findResponse(ctx));
	}
	

	private static void findResponse(Context ctx) {
		Map<String, String> res = locateResponse(ctx.req);
		if(res != null) {
			ctx.result(res.get("Response"));
			String headers = res.get("ResponseHeaders");
			if(headers != null && !headers.isEmpty()) {
				String[] lines = headers.split("\\r?\\n");
				if(lines.length > 0) {
					for(String line : lines) {
						String[] header = line.split(":");
						if(header.length > 1) {
							ctx.res.addHeader(header[0].trim(), header[1].trim());
						}
					}
				}
			}
		} else {
			ctx.status(404);
		}
	}

	private static Map<String, String> locateResponse(HttpServletRequest request) {
		String result = null;
		try {
			Map<String, String> target = null;
			Map<String, String> defaultTarget = null;
			List<Map<String, String>> rows = ExcelUtilityFunction.getRowsURLStartsWith(dataSourceDirectoryExcel,
					SHEET_NAME, request.getRequestURI());
			for (Map<String, String> row : rows) {
				if (request.getMethod().equalsIgnoreCase(row.get("Method"))) {
					String assertStatement = row.get("RequestAssert");
					if (assertStatement == null || assertStatement.equals("")) {
						defaultTarget = row;
					} else {
						if (assertStatement != null && !"".equals(assertStatement)) {
							String targetString = getTargetString(assertStatement, request);
							String condition1 = row.get("RequestCondition1");
							String andOr = row.get("RequestConditionAppend1");
							String condition2 = row.get("RequestCondition2");
							String value1 = row.get("ConditionValue1");
							String value2 = row.get("ConditionValue2");
							if (checkTarget(targetString, condition1, andOr, condition2, value1, value2)) {
								target = row;
								break;
							}
						}
					}
				}
			}
			if(target != null) {
				return target;
			} else { 
				if(defaultTarget != null) {
					return defaultTarget;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static boolean checkTarget(String targetString, String condition1, String andOr, String condition2,
			String value1, String value2) {
		boolean checkTarget1 = false;
		boolean checkTarget2 = false;

		if (condition1.equals("contains")) {
			checkTarget1 = targetString.contains(value1);
		} else if (condition1.equals("containsIgnoreCase")) {
			checkTarget1 = targetString.toLowerCase().contains(value1.toLowerCase());
		} else if (condition1.equals("equals")) {
			checkTarget1 = targetString.equals(value1);
		} else if (condition1.equals("equalsIgnoreCase")) {
			checkTarget1 = targetString.equalsIgnoreCase(value1);
		}

		if (andOr != null && (andOr.equals("AND") || andOr.equals("OR"))) {
			if (condition2.equals("contains")) {
				checkTarget2 = targetString.contains(value2);
			} else if (condition2.equals("containsIgnoreCase")) {
				checkTarget2 = targetString.toLowerCase().contains(value2.toLowerCase());
			} else if (condition2.equals("equals")) {
				checkTarget2 = targetString.equals(value2);
			} else if (condition2.equals("equalsIgnoreCase")) {
				checkTarget2 = targetString.equalsIgnoreCase(value2);
			}

			if (andOr.equals("AND")) {
				return checkTarget1 && checkTarget2;
			}
			if (andOr.equals("OR")) {
				return checkTarget1 || checkTarget2;
			}
		}

		return checkTarget1;
	}

	private static String getTargetString(String assertStatemet, HttpServletRequest request) throws IOException {
		String targetString = "";
		switch (assertStatemet) {
		case "URL": {
			targetString = request.getRequestURI() + "?" + request.getQueryString();
			break;
		}
		case "Header": {
			Map<String, String> headers = Collections.list(((HttpServletRequest) request).getHeaderNames()).stream()
					.collect(Collectors.toMap(h -> h, ((HttpServletRequest) request)::getHeader));
			targetString = headers.toString();
			break;
		}
		case "Cookie": {
			targetString = request.getHeader("Cookie");
			break;
		}
		case "Body": {
			targetString = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			break;
		}
		}
		return targetString;
	}
}
